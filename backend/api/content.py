from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel
from services import git_service, org_service
from services.auth_service import get_current_user, require_admin

router = APIRouter()


class WriteBody(BaseModel):
    content: str
    message: str = "Update content"


@router.get("/content/list")
def list_content():
    return git_service.list_files()


@router.get("/content/{path:path}")
def get_content(path: str):
    try:
        raw = git_service.read_file(path)
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="File not found")
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return {"raw": raw, "html": org_service.parse_to_html(raw)}


@router.put("/content/{path:path}")
def put_content(path: str, body: WriteBody, request: Request):
    user = get_current_user(request)
    require_admin(user)
    try:
        git_service.write_and_commit(
            path, body.content, body.message, author=user.username
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return {"status": "ok"}
