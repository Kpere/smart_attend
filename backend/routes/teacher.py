from flask import Blueprint, request, jsonify
from functools import wraps
import jwt
from flask import current_app
from models import db, User, Subject, Enrollment, Session
import random
import string
from datetime import datetime, timedelta

teacher_bp = Blueprint('teacher', __name__)

def teacher_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'error': 'Token is missing'}), 401
            
        try:
            token = token.split(' ')[1]
            data = jwt.decode(token, current_app.config['SECRET_KEY'], algorithms=['HS256'])
            current_user = User.query.get(data['user_id'])
            if not current_user or current_user.role != 'teacher':
                return jsonify({'error': 'Teacher privileges required'}), 403
        except Exception as e:
            return jsonify({'error': str(e)}), 401
            
        return f(current_user, *args, **kwargs)
    return decorated

@teacher_bp.route('/subjects', methods=['POST'])
@teacher_required
def create_subject(current_user):
    data = request.get_json()
    name = data.get('name')
    code = data.get('code')
    credit_hours = data.get('credit_hours', 3)

    if not name or not code:
        return jsonify({'error': 'Name and code are required'}), 400

    # Validate credit_hours is a positive integer
    try:
        credit_hours = int(credit_hours)
        if credit_hours < 1:
            credit_hours = 3
    except (ValueError, TypeError):
        credit_hours = 3

    # Check if subject code already exists
    existing = Subject.query.filter_by(code=code).first()
    if existing:
        return jsonify({'error': 'Subject code already exists'}), 400

    new_subject = Subject(
        name=name,
        code=code,
        teacher_id=current_user.id,
        credit_hours=credit_hours
    )
    db.session.add(new_subject)
    db.session.commit()

    return jsonify({
        'message': 'Subject created successfully',
        'subject': {
            'id': new_subject.id,
            'name': new_subject.name,
            'code': new_subject.code,
            'credit_hours': new_subject.credit_hours,
            'teacher_name': current_user.name
        }
    }), 201

@teacher_bp.route('/subjects', methods=['GET'])
@teacher_required
def get_teacher_subjects(current_user):
    """Return all school subjects — subjects are globally managed, not per-teacher."""
    subjects = Subject.query.all()
    result = []
    for sub in subjects:
        teacher_name = "Unassigned"
        is_mine = False
        if sub.teacher:
            teacher_name = sub.teacher.name
            if sub.teacher_id == current_user.id:
                is_mine = True
        result.append({
            'id': sub.id,
            'name': sub.name,
            'code': sub.code,
            'credit_hours': sub.credit_hours,
            'teacher_name': teacher_name,
            'is_mine': is_mine
        })
    return jsonify(result), 200

@teacher_bp.route('/subjects/<int:subject_id>/claim', methods=['POST'])
@teacher_required
def claim_subject(current_user, subject_id):
    subject = Subject.query.get(subject_id)
    if not subject:
        return jsonify({'error': 'Subject not found'}), 404
        
    if subject.teacher_id is not None:
        return jsonify({'error': 'Subject is already assigned to a teacher'}), 400
        
    subject.teacher_id = current_user.id
    db.session.commit()
    
    return jsonify({
        'message': 'Subject claimed successfully',
        'subject': {
            'id': subject.id,
            'name': subject.name,
            'code': subject.code,
            'credit_hours': subject.credit_hours,
            'teacher_name': current_user.name
        }
    }), 200

@teacher_bp.route('/subjects/<int:subject_id>', methods=['DELETE'])
@teacher_required
def delete_subject(current_user, subject_id):
    subject = Subject.query.filter_by(id=subject_id, teacher_id=current_user.id).first()
    if not subject:
        return jsonify({'error': 'Subject not found or access denied'}), 404

    # Instead of deleting the subject (which destroys it for the whole school), we un-claim it.
    subject.teacher_id = None
    db.session.commit()
    return jsonify({'message': 'Subject unclaimed successfully'}), 200

@teacher_bp.route('/sessions/start', methods=['POST'])
@teacher_required
def start_session(current_user):
    data = request.get_json()
    subject_id = data.get('subject_id')

    if not subject_id:
        return jsonify({'error': 'subject_id is required'}), 400

    subject = Subject.query.filter_by(id=subject_id, teacher_id=current_user.id).first()
    if not subject:
        return jsonify({'error': 'Subject not found or you do not own it'}), 404

    # Close any existing active sessions for this subject
    existing_sessions = Session.query.filter_by(subject_id=subject_id, status='active').all()
    for s in existing_sessions:
        s.status = 'closed'

    # Generate random 4-letter uppercase code
    code = ''.join(random.choices(string.ascii_uppercase, k=4))
    
    # Expiry 10 minutes from now
    expires_at = datetime.utcnow() + timedelta(minutes=10)

    new_session = Session(
        subject_id=subject_id,
        teacher_id=current_user.id,
        code=code,
        expires_at=expires_at,
        status='active'
    )
    db.session.add(new_session)
    db.session.commit()

    return jsonify({
        'message': 'Session started',
        'code': code,
        'expires_at': expires_at.isoformat()
    }), 201

@teacher_bp.route('/students', methods=['GET'])
@teacher_required
def get_teacher_students(current_user):
    from models import Attendance, Session # Ensure these are imported if not already

    subjects = Subject.query.filter_by(teacher_id=current_user.id).all()
    subject_ids = [s.id for s in subjects]

    if not subject_ids:
        return jsonify([]), 200

    enrollments = Enrollment.query.filter(Enrollment.subject_id.in_(subject_ids)).all()

    result = []
    for enr in enrollments:
        total_sessions = Session.query.filter_by(subject_id=enr.subject.id).count()
        if total_sessions > 0:
            attended = Attendance.query.join(Session).filter(
                Attendance.student_id == enr.student.id,
                Session.subject_id == enr.subject.id,
                Attendance.status == 'present'
            ).count()
            attendance_percentage = int((attended / total_sessions) * 100)
        else:
            attendance_percentage = 0

        result.append({
            'student_id': enr.student.id,
            'student_name': enr.student.name,
            'student_email': enr.student.email,
            'subject_id': enr.subject.id,
            'subject_name': enr.subject.name,
            'enrollment_status': enr.status,
            'attendance_percentage': attendance_percentage
        })
    return jsonify(result), 200
