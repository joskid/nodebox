from nodebox.node import Node
from nodebox import graphics

@GeometryFilter(image="rect.png")
def rect(x=0.0, y=0.0, width=100.0, height=100.0, rx=0.0, ry=0.0, fill=Color(), stroke=Color(), strokeWidth=0.0):
    """Create rectangles and rounded rectangles."""
    p = graphics.Path()
    if rx == 0 and ry == 0:
        p.rect(x, y, width, height)
    else:
        p.roundedRect(x, y, width, height, rx, ry)
    p.fillColor = fill
    if strokeWidth > 0:
        p.strokeColor = stroke
        p.strokeWidth = strokeWidth
    else:
        p.strokeColor = None
    return p.asGeometry()


