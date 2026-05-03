import orgparse


def parse_to_html(org_text: str) -> str:
    doc = orgparse.loads(org_text)
    parts: list[str] = []

    def _render_node(node) -> None:
        if hasattr(node, "heading") and node.heading:
            level = node.level
            parts.append(f"<h{level}>{node.heading}</h{level}>")
        body = node.body.strip() if node.body else ""
        if body:
            for line in body.splitlines():
                parts.append(f"<p>{line}</p>" if line.strip() else "")

    _render_node(doc)
    for node in doc.children:
        _render_node(node)

    return "\n".join(parts)
