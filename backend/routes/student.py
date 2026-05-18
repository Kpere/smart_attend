from flask import Blueprint, request, jsonify
from functools import wraps
import jwt
from flask import current_app
from models import db, User, Subject, Enrollment

student_bp = Blueprint('student', __name__)

def student_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'error': 'Token is missing'}), 401
            
        try:
            token = token.split(' ')[1]
            data = jwt.decode(token, current_app.config['SECRET_KEY'], algorithms=['HS256'])
            current_user = User.query.get(data['user_id'])
            if not current_user or current_user.role != 'student':
                return jsonify({'error': 'Student privileges required'}), 403
        except Exception as e:
            return jsonify({'error': str(e)}), 401
            
        return f(current_user, *args, **kwargs)
    return decorated

@student_bp.route('/subjects', methods=['GET'])
@student_required
def list_all_subjects(current_user):
    """All subjects (for the Subjects tab browse & enroll screen)."""
    subjects = Subject.query.all()
    result = []
    for sub in subjects:
        teacher_name = "Unassigned"
        if sub.teacher:
            teacher_name = sub.teacher.name.split(' ')[0]

        enrollment = Enrollment.query.filter_by(student_id=current_user.id, subject_id=sub.id).first()
        is_enrolled = enrollment is not None

        result.append({
            'id': sub.id,
            'name': sub.name,
            'code': sub.code,
            'credit_hours': sub.credit_hours,
            'teacher_name': teacher_name,
            'is_enrolled': is_enrolled
        })
    return jsonify(result), 200

@student_bp.route('/enrolled-subjects', methods=['GET'])
@student_required
def get_enrolled_subjects(current_user):
    """Only subjects the student is enrolled in — used for the attendance dropdown."""
    enrollments = Enrollment.query.filter_by(student_id=current_user.id).all()
    result = []
    for enr in enrollments:
        sub = enr.subject
        teacher_name = "Unassigned"
        if sub.teacher:
            teacher_name = sub.teacher.name

        result.append({
            'id': sub.id,
            'name': sub.name,
            'code': sub.code,
            'credit_hours': sub.credit_hours,
            'teacher_name': teacher_name,
            'is_enrolled': True
        })
    return jsonify(result), 200

@student_bp.route('/enroll', methods=['POST'])
@student_required
def enroll_in_subject(current_user):
    data = request.get_json()
    subject_id = data.get('subject_id')

    if not subject_id:
        return jsonify({'error': 'subject_id is required'}), 400

    subject = Subject.query.get(subject_id)
    if not subject:
        return jsonify({'error': 'Subject not found'}), 404

    existing = Enrollment.query.filter_by(student_id=current_user.id, subject_id=subject_id).first()
    if existing:
        return jsonify({'error': 'Already enrolled in this subject'}), 400

    enrollment = Enrollment(student_id=current_user.id, subject_id=subject_id, status='approved')
    db.session.add(enrollment)
    db.session.commit()

    return jsonify({'message': 'Enrolled successfully'}), 200
