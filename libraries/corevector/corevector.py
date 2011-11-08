from nodebox.graphics import Path

def rect(position, width, height, roundness):
    p = Path()
    if not (roundness.x or roundness.y):
        p.rect(position.x, position.y, width, height)
    else:
        p.roundedRect(position.x, position.y, width, height, roundness.x, roundness.y)
    return p.asGeometry()

def color(shape, fill, stroke, strokeWidth):
    if shape is None: return None
    new_shape = shape.clone()
    for path in new_shape.paths:
        path.fillColor = fill
        if strokeWidth > 0:
            path.strokeColor = stroke
            path.strokeWidth = strokeWidth
        else:
            path.strokeColor = None
    return new_shape
    
    
