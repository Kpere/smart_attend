from flask import Blueprint, request, jsonify
from functools import wraps
import jwt
from flask import current_app
from models import db, User, Subject, Enrollment

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
        if sub.teacher:
            teacher_name = sub.teacher.name
        result.append({
            'id': sub.id,
            'name': sub.name,
            'code': sub.code,
            'credit_hours': sub.credit_hours,
            'teacher_name': teacher_name
        })
    return jsonify(result), 200

@teacher_bp.route('/subjects/<int:subject_id>', methods=['DELETE'])
@teacher_required
def delete_subject(current_user, subject_id):
    subject = Subject.query.filter_by(id=subject_id, teacher_id=current_user.id).first()
    if not subject:
        return jsonify({'error': 'Subject not found or access denied'}), 404

    # Delete related enrollments first
    Enrollment.query.filter_by(subject_id=subject_id).delete()
    db.session.delete(subject)
    db.session.commit()
    return jsonify({'message': 'Subject deleted successfully'}), 200

@teacher_bp.route('/students', methods=['GET'])
@teacher_required
def get_teacher_students(current_user):
    subjects = Subject.query.filter_by(teacher_id=current_user.id).all()
    subject_ids = [s.id for s in subjects]

    if not subject_ids:
        return jsonify([]), 200

    enrollments = Enrollment.query.filter(Enrollment.subject_id.in_(subject_ids)).all()

    result = []
    for enr in enrollments:
        result.append({
            'student_id': enr.student.id,
            'student_name': enr.student.name,
            'student_email': enr.student.email,
            'subject_id': enr.subject.id,
            'subject_name': enr.subject.name,
            'enrollment_status': enr.status
        })
    return jsonify(result), 200
