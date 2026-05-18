from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

db = SQLAlchemy()

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    google_id = db.Column(db.String(100), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    name = db.Column(db.String(100), nullable=False)
    role = db.Column(db.String(20), default='pending') # admin, teacher, student, pending
    status = db.Column(db.String(20), default='active') # active, suspended
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def to_dict(self):
        return {
            'id': self.id,
            'email': self.email,
            'name': self.name,
            'role': self.role,
            'status': self.status,
            'created_at': self.created_at.isoformat()
        }

class Course(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    code = db.Column(db.String(20), unique=True, nullable=False)
    department = db.Column(db.String(50))
    status = db.Column(db.String(20), default='pending') # pending, approved, rejected, archived

class Subject(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    code = db.Column(db.String(20), unique=True, nullable=False)
    course_id = db.Column(db.Integer, db.ForeignKey('course.id'), nullable=True)
    teacher_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    credit_hours = db.Column(db.Integer, default=3)
    course = db.relationship('Course', backref=db.backref('subjects', lazy=True))
    teacher = db.relationship('User', backref=db.backref('taught_subjects', lazy=True))

class Enrollment(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    student_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    subject_id = db.Column(db.Integer, db.ForeignKey('subject.id'), nullable=False)
    status = db.Column(db.String(20), default='pending') # pending, approved, rejected
    student = db.relationship('User', backref=db.backref('enrollments', lazy=True))
    subject = db.relationship('Subject', backref=db.backref('enrolled_students', lazy=True))

class Session(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    subject_id = db.Column(db.Integer, db.ForeignKey('subject.id'), nullable=False)
    teacher_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    code = db.Column(db.String(6), unique=True, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    expires_at = db.Column(db.DateTime, nullable=False)
    status = db.Column(db.String(20), default='active') # active, closed
    subject = db.relationship('Subject', backref=db.backref('sessions', lazy=True))

class Attendance(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    session_id = db.Column(db.Integer, db.ForeignKey('session.id'), nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    status = db.Column(db.String(20), default='present') # present, absent, late
    marked_at = db.Column(db.DateTime, default=datetime.utcnow)
    session = db.relationship('Session', backref=db.backref('attendances', lazy=True))
    student = db.relationship('User', backref=db.backref('attendance_records', lazy=True))
