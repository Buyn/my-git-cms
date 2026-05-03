from fastapi import APIRouter, HTTPException, Request, Depends
from pydantic import BaseModel, EmailStr
from sqlalchemy.orm import Session
from models.db import get_db
from models.comment import Comment, Reaction
from services.auth_service import get_current_user

router = APIRouter()


class CommentCreate(BaseModel):
    page_path: str
    content: str
    anon_email: EmailStr | None = None


class CommentUpdate(BaseModel):
    content: str


class ReactionCreate(BaseModel):
    type: str


@router.get("/comments/{page_path:path}")
def list_comments(page_path: str, db: Session = Depends(get_db)):
    comments = db.query(Comment).filter(Comment.page_path == page_path).all()
    return [
        {
            "id": c.id,
            "author_id": c.author_id,
            "anon_email": c.anon_email,
            "content": c.content,
            "page_path": c.page_path,
            "created_at": c.created_at,
            "updated_at": c.updated_at,
            "reactions": [{"id": r.id, "type": r.type, "user_id": r.user_id} for r in c.reactions],
        }
        for c in comments
    ]


@router.post("/comments", status_code=201)
def create_comment(body: CommentCreate, request: Request, db: Session = Depends(get_db)):
    user = get_current_user(request)
    if user is None and not body.anon_email:
        raise HTTPException(status_code=400, detail="anon_email required for unauthenticated comments")
    comment = Comment(
        author_id=user.id if user else None,
        anon_email=None if user else str(body.anon_email),
        content=body.content,
        page_path=body.page_path,
    )
    db.add(comment)
    db.commit()
    db.refresh(comment)
    return {"id": comment.id}


@router.put("/comments/{comment_id}")
def update_comment(comment_id: int, body: CommentUpdate, request: Request, db: Session = Depends(get_db)):
    user = get_current_user(request)
    comment = db.get(Comment, comment_id)
    if comment is None:
        raise HTTPException(status_code=404, detail="Comment not found")
    if user is None or comment.author_id != user.id:
        raise HTTPException(status_code=403, detail="Forbidden")
    comment.content = body.content
    db.commit()
    return {"status": "ok"}


@router.delete("/comments/{comment_id}", status_code=204)
def delete_comment(comment_id: int, request: Request, db: Session = Depends(get_db)):
    user = get_current_user(request)
    comment = db.get(Comment, comment_id)
    if comment is None:
        raise HTTPException(status_code=404, detail="Comment not found")
    if user is None or (comment.author_id != user.id and user.role != "admin"):
        raise HTTPException(status_code=403, detail="Forbidden")
    db.delete(comment)
    db.commit()


@router.post("/comments/{comment_id}/reaction", status_code=201)
def add_reaction(comment_id: int, body: ReactionCreate, request: Request, db: Session = Depends(get_db)):
    user = get_current_user(request)
    if user is None:
        raise HTTPException(status_code=401, detail="Login required")
    comment = db.get(Comment, comment_id)
    if comment is None:
        raise HTTPException(status_code=404, detail="Comment not found")
    existing = db.query(Reaction).filter_by(user_id=user.id, comment_id=comment_id).first()
    if existing:
        existing.type = body.type
        db.commit()
        return {"id": existing.id}
    reaction = Reaction(user_id=user.id, comment_id=comment_id, type=body.type)
    db.add(reaction)
    db.commit()
    db.refresh(reaction)
    return {"id": reaction.id}
