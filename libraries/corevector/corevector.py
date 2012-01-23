from math import pi, sin, cos, radians
from nodebox.graphics import Geometry, Path, Color, Transform, Text, Point
from nodebox.util.Geometry import coordinates, angle, distance

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
    
def line(position, distance, angle):
    p = Path()
    x1, y1 = coordinates(position.x, position.y, distance, angle)
    p.line(position.x, position.y, x1, y1)
    p.strokeColor = Color.BLACK
    p.strokeWidth = 1
    return p.asGeometry()

def rect(position, width, height, roundness):
    p = Path()
    if not (roundness.x or roundness.y):
        p.rect(position.x, position.y, width, height)
    else:
        p.roundedRect(position.x, position.y, width, height, roundness.x, roundness.y)
    return p.asGeometry()

def ellipse(position, width, height):
    p = Path()
    p.ellipse(position.x, position.y, width, height)
    return p.asGeometry()

def polygon(position, radius, sides, align):
    p = Path()
    x, y, r = position.x, position.y, radius
    sides = max(sides, 3)
    a = 360.0 / sides
    da = 0
    if align:
        x0, y0 = coordinates(x, y, r, 0)
        x1, y1 = coordinates(x, y, r, a)
        da = -angle(x1, y1, x0, y0)
    for i in xrange(sides):
        x1, y1 = coordinates(x, y, r, (a*i) + da)
        if i == 0:
            p.moveto(x1, y1)
        else:
            p.lineto(x1, y1)
    p.close()
    return p.asGeometry()

def star(position, points, outer, inner):
    p = Path()
    p.moveto(position.x, position.y + outer / 2)

    # Calculate the points of the star.
    for i in xrange(1, points * 2):
        angle = i * pi / points
        radius = i % 2 and inner / 2 or outer / 2
        x = position.x + radius * sin(angle)
        y = position.y + radius * cos(angle)
        p.lineto(x, y)
    p.close()
    return p.asGeometry()

def textpath(text, position, width, height, font, fontSize, align):
    t = Text(text, position.x, position.y, width, height)
    t.fontName = font
    t.fontSize = fontSize
    # valueOf requires a correct value: LEFT, CENTER, RIGHT or JUSTIFY. Anything else will
    # make it crash. If users start doing crazy things and change the alignment, at least
    # make sure you catch the error.
    try:
        t.align = Text.Align.valueOf(align)
    except:
        pass
    return t.path.asGeometry()
    
def transform(shape, order, translation, rotation, scale):
    if shape is None: return None
    t = Transform()
    # Each letter of the order describes an operation.
    for op in order:
        if op == 't':
            t.translate(translation.x, translation.y)
        elif op == 'r':
            t.rotate(rotation)
        elif op == 's':
            t.scale(scale.x / 100, scale.y / 100)
    # Transform.map clones and transforms the geometry.
    return t.map(shape)

def fit(shape, position, width, height, keepProportions):
    if shape is None: return None

    px, py, pw, ph = list(shape.bounds)

    # Make sure pw and ph aren't infinitely small numbers.
    # This will lead to incorrect transformations with for examples lines.
    if 0 < pw <= 0.000000000001: pw = 0
    if 0 < ph <= 0.000000000001: ph = 0

    t = Transform()
    t.translate(position.x, position.y)
    if keepProportions:
        # Don't scale widths or heights that are equal to zero.
        w = pw and width / pw or float("inf")
        h = ph and height / ph or float("inf")
        w = h = min(w, h)
    else:
        # Don't scale widths or heights that are equal to zero.
        w = pw and width / pw or 1
        h = ph and height / ph or 1
    t.scale(w, h)
    t.translate(-pw / 2 - px, -ph / 2 - py)

    return t.map(shape)
    
def copy(shape, copies, order, translation, rotation, scale):
    if shape is None:
        return None
    g = Geometry()
    tx = ty = r = 0.0
    sx = sy = 1.0
    for i in xrange(copies):
        t = Transform()
        # Each letter of the order describes an operation.
        for op in order:
            if op == 't':
                t.translate(tx, ty)
            elif op == 'r':
                t.rotate(r)
            elif op == 's':
                t.scale(sx, sy)
        new_shape = t.map(shape)
        g.extend(new_shape)
        tx += translation.x
        ty += translation.y
        r += rotation
        sx += scale.x / 100.0
        sy += scale.y / 100.0
    return g

def makePoint(x, y):
    return Point(x, y)

def grid(*args):
    try:
        width, height, rows, columns, position = [arg[0] for arg in args]
    except IndexError:
        return []
    except ValueError:
        return []
        
    if columns > 1:
        column_size = width / (columns - 1)
        left = position.x - width / 2
    else:
        column_size = left = 0
    if rows > 1:
        row_size = height / (rows - 1)
        top = position.y - height / 2
    else:
        row_size = top = 0
        
    points = []
    for ri in xrange(rows):
        for ci in xrange(columns):
            x = left + ci * column_size
            y = top + ri * row_size
            points.append(Point(x, y))
    return points
