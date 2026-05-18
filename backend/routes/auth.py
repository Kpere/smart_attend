from flask import request, jsonify, current_app
from . import auth_bp
from models import db, User
from google.oauth2 import id_token
from google.auth.transport import requests
import jwt
from datetime import datetime, timedelta

@auth_bp.route('/google', methods=['POST'])
def google_auth():
    data = request.get_json()
    token = data.get('id_token')
    
    if not token:
        return jsonify({'error': 'No token provided'}), 400
        
    try:
        # Verify the Google token
        idinfo = id_token.verify_oauth2_token(
            token, requests.Request(), current_app.config['GOOGLE_CLIENT_ID'], clock_skew_in_seconds=10
        )
        
        email = idinfo['email']
        name = idinfo.get('name', 'Unknown User')
        google_id = idinfo['sub']
        
        # Check if user exists
        user = User.query.filter_by(email=email).first()
        
        if not user:
            requested_role = data.get('requested_role', 'student')
            
            # Check if this is the very first user in the database
            is_first_user = User.query.first() is None
            
            final_role = 'admin' if is_first_user else requested_role
            final_status = 'active'
            
            if not is_first_user and requested_role == 'admin':
                final_status = 'pending'
            else:
                final_status = 'active'
            
            # Create new user
            user = User(google_id=google_id, email=email, name=name, role=final_role, status=final_status)
            db.session.add(user)
            db.session.commit()
        
        if user.status == 'suspended':
            return jsonify({'error': 'Account suspended'}), 403
            
        # Generate our own JWT
        jwt_token = jwt.encode({
            'user_id': user.id,
            'role': user.role,
            'exp': datetime.utcnow() + timedelta(days=7)
        }, current_app.config['SECRET_KEY'], algorithm='HS256')
        
        # Determine effective role for the app routing
        effective_role = 'pending' if user.status == 'pending' else user.role
        
        return jsonify({
            'user_id': user.id,
            'role': effective_role,
            'status': user.status,
            'jwt_token': jwt_token
        }), 200
        
    except ValueError as e:
        print("ValueError in Google Auth:", str(e))
        return jsonify({'error': 'Invalid token', 'details': str(e)}), 401
    except Exception as e:
        return jsonify({'error': str(e)}), 500
