from flask import Flask, jsonify
from flask_cors import CORS
from config import Config
from models import db

def create_app(config_class=Config):
    app = Flask(__name__)
    app.config.from_object(config_class)
    
    CORS(app)
    db.init_app(app)
    
    from routes.auth import auth_bp
    from routes.admin import admin_bp
    from routes.teacher import teacher_bp
    from routes.student import student_bp
    
    app.register_blueprint(auth_bp, url_prefix='/api/auth')
    app.register_blueprint(admin_bp, url_prefix='/api/admin')
    app.register_blueprint(teacher_bp, url_prefix='/api/teacher')
    app.register_blueprint(student_bp, url_prefix='/api/student')
    
    @app.route('/health', methods=['GET'])
    def health_check():
        return jsonify({'status': 'ok'}), 200

    with app.app_context():
        db.create_all()
        seed_subjects()

    return app


def seed_subjects():
    """Seed the 4 core subjects if none exist yet."""
    from models import Subject
    if Subject.query.count() == 0:
        subjects = [
            Subject(name='Data Mining',                code='DM001',   credit_hours=3),
            Subject(name='Web Development',            code='CSE201',  credit_hours=4),
            Subject(name='Operating System',           code='CSE350',  credit_hours=3),
            Subject(name='Object Oriented Programming',code='CSIT351', credit_hours=3),
        ]
        db.session.bulk_save_objects(subjects)
        db.session.commit()
        print("[seed] 4 subjects inserted successfully.")
    else:
        print("[seed] Subjects already exist — skipping seed.")


# Instantiate the app globally so Gunicorn can find it easily with "gunicorn app:app"
app = create_app()

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True, port=5000)
