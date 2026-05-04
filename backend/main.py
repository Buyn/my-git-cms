import os
from pathlib import Path
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from dotenv import load_dotenv

load_dotenv()

from models.db import Base, engine
from models import user, comment  # noqa: F401 — register models
from api import content, comments, auth

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Git CMS")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(content.router)
app.include_router(comments.router)
app.include_router(auth.router)

media_path = Path(os.getenv("MEDIA_PATH", "../media")).resolve()
media_path.mkdir(parents=True, exist_ok=True)
app.mount("/media", StaticFiles(directory=str(media_path)), name="media")
