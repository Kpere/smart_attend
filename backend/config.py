import os

class Config:
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'super-secret-key-change-in-production'
    
    # Render's Postgres URLs start with postgres://, but SQLAlchemy requires the psycopg dialect for python 3.14
    db_url = os.environ.get('DATABASE_URL')
    if db_url and db_url.startswith("postgres://"):
        db_url = db_url.replace("postgres://", "postgresql+psycopg://", 1)
    elif db_url and db_url.startswith("postgresql://"):
        db_url = db_url.replace("postgresql://", "postgresql+psycopg://", 1)
        
    SQLALCHEMY_DATABASE_URI = db_url or 'sqlite:///smartattend.db'
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    GOOGLE_CLIENT_ID = "718703137342-sbsua5i7iojhqmqh3uu21coi1vb131ce.apps.googleusercontent.com"
