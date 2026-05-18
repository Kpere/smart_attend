from flask import Blueprint, request, jsonify
from models import db, User, Subject

admin_bp = Blueprint('admin', __name__)

@admin_bp.route('/stats', methods=['GET'])
def get_dashboard_stats():
    total_students = User.query.filter_by(role='student').count()
    total_teachers = User.query.filter_by(role='teacher').count()
    pending_approvals = User.query.filter_by(role='pending').count()
    total_subjects = Subject.query.count()
    
    return jsonify({
        'total_students': total_students,
        'total_teachers': total_teachers,
        'pending_approvals': pending_approvals,
        'total_subjects': total_subjects
    }), 200

@admin_bp.route('/users', methods=['GET'])
def get_users():
    users = User.query.all()
    return jsonify([user.to_dict() for user in users]), 200

@admin_bp.route('/users/<int:user_id>', methods=['PATCH'])
def update_user(user_id):
    user = User.query.get(user_id)
    if not user:
        return jsonify({'error': 'User not found'}), 404

    data = request.get_json()

    if 'role' in data:
        user.role = data['role']
    if 'status' in data:
        user.status = data['status']

    db.session.commit()
    return jsonify(user.to_dict()), 200

@admin_bp.route('/users/<int:user_id>', methods=['DELETE'])
def delete_user(user_id):
    user = User.query.get(user_id)
    if not user:
        return jsonify({'error': 'User not found'}), 404

    db.session.delete(user)
    db.session.commit()
    return jsonify({'message': 'User deleted successfully'}), 200

@admin_bp.route('/subjects', methods=['GET'])
def get_all_subjects():
    subjects = Subject.query.all()
    result = []
    for sub in subjects:
        teacher_name = sub.teacher.name if sub.teacher else 'Unassigned'
        result.append({
            'id': sub.id,
            'name': sub.name,
            'code': sub.code,
            'credit_hours': sub.credit_hours,
            'teacher_name': teacher_name
        })
    return jsonify(result), 200

@admin_bp.route('/subjects', methods=['POST'])
def create_subject_admin():
    data = request.get_json()
    name = data.get('name')
    code = data.get('code')
    teacher_id = data.get('teacher_id')
    credit_hours = data.get('credit_hours', 3)

    if not name or not code:
        return jsonify({'error': 'Name and code are required'}), 400

    existing = Subject.query.filter_by(code=code).first()
    if existing:
        return jsonify({'error': 'Subject code already exists'}), 400

    new_subject = Subject(
        name=name,
        code=code,
        teacher_id=teacher_id,
        credit_hours=int(credit_hours)
    )
    db.session.add(new_subject)
    db.session.commit()
    return jsonify({'message': 'Subject created', 'id': new_subject.id}), 201

@admin_bp.route('/subjects/<int:subject_id>', methods=['DELETE'])
def delete_subject(subject_id):
    subject = Subject.query.get(subject_id)
    if not subject:
        return jsonify({'error': 'Subject not found'}), 404

    db.session.delete(subject)
    db.session.commit()
    return jsonify({'message': 'Subject deleted successfully'}), 200
