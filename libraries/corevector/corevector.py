from math import pi, sin, cos, radians
from random import seed as _seed, uniform

from java.awt.geom import Arc2D

from nodebox.graphics import Geometry, Path, Contour, Color, Transform, Text, Point, Rect
from nodebox.util.Geometry import coordinates, angle, distance

def align(shape, x, y, halign="center", valign="middle"):
    """Align a shape in relation to the origin."""
    if shape is None: return None
    new_shape = shape.clone()
    if halign == "left":
        dx = x - new_shape.bounds.x
    elif halign == "right":
        dx = x - new_shape.bounds.x - new_shape.bounds.width
    elif halign == "center":
        dx = x - new_shape.bounds.x - new_shape.bounds.width / 2
    else:
        dx = 0
    if valign == "top":
        dy = y - new_shape.bounds.y
    elif valign == "bottom":
        dy = y - new_shape.bounds.y - new_shape.bounds.height
    elif valign == "middle":
        dy = y - new_shape.bounds.y - new_shape.bounds.height / 2
    else:
        dy = 0
    new_shape.translate(dx, dy)
    return new_shape

def arc(position, width, height, start_angle, degrees, arc_type):
    """Create an arc."""
    if arc_type == "chord":
        awt_type = Arc2D.CHORD
    elif arc_type == "pie":
        awt_type = Arc2D.PIE
    else:
        awt_type = Arc2D.OPEN
    p = Path(Arc2D.Double(position.x-width/2, position.y-height/2, width, height, -start_angle, -degrees, awt_type))
    return p

def color(shape, fill, stroke, strokeWidth):
    """Change the color of the input shape."""
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

def compound(shape1, shape2, function="united", invert_difference=False):
    """Add, subtract or intersect geometry."""
    if shape1 is None: return None
    if shape2 is None: return shape1.clone()
    # The invert turns the operation around.
    if invert_difference:
        shape1, shape2 = shape2, shape1
    
    # We're not changing the original geometry so there is no need to clone.
    # Unite all the paths from geometry A.
    compound1 = reduce(lambda p1, p2: p1.united(p2), shape1.paths, None)
    # Unite all the paths from geometry B.
    compound2 = reduce(lambda p1, p2: p1.united(p2), shape2.paths, None)

    # Final check to see if the two compound paths contain data.
    if compound1 is None or compound2 is None: return None
    # Combine the two compound paths using the given function.
    if function == "united":
        return compound1.united(compound2)
    elif f == "subtracted":
        return compound1.subtracted(compound2)
    elif f == "intersected":
        return compound1.intersected(compound2)
    else:
        return None

def connect(shape, closed=True):
    """Connects all points in a path."""
    if shape is None: return None
    if shape.pointCount < 2: return None
    start = shape.points[0]
    p = Path()
    p.moveto(start.x, start.y)
    for point in shape.points[1:]:
        p.lineto(point.x, point.y)
    if closed:
        p.close()
    p.stroke = Color.BLACK
    p.strokeWidth = 1.0
    return p

def copy(shape, copies, transform_order='tsr', translate=Point.ZERO, rotate=0, scale=Point.ZERO):
    """Create multiple copies of a shape."""
    if shape is None: return None
    g = Geometry()
    tx = ty = r = 0.0
    sx = sy = 1.0
    for i in xrange(copies):
        t = Transform()
        # Each letter of the order describes an operation.
        for op in transform_order:
            if op == 't':
                t.translate(tx, ty)
            elif op == 'r':
                t.rotate(r)
            elif op == 's':
                t.scale(sx, sy)
        new_shape = t.map(shape)
        g.extend(new_shape)
        tx += translate.x
        ty += translate.y
        r += rotate
        sx += scale.x / 100.0
        sy += scale.y / 100.0
    return g

def curve(path_data):
    import svg
    if not path_data: return None
    return svg.path_from_string(path_data)

def delete_bounding(shape, bounding, scope="points", delete_selected=True):
    """Delete points or paths that lie within the bounding path."""
    if shape is None: return None
    # We're going to reconstruct the entire geometry, 
    # leaving out the points we don't need.
    if scope == "points":
        new_geo = Geometry()
        for old_path in shape.paths:
            new_path = Path(old_path, False) # cloneContours = False
            for old_contour in old_path.contours:
                new_contour = Contour()
                for point in old_contour.points:
                    if bounding.contains(point) == delete_selected:
                        new_contour.addPoint(point.x, point.y)
                new_path.add(new_contour)
            new_geo.add(new_path)    
        return new_geo
    elif scope == "paths":
        new_geo = Geometry()
        for old_path in shape.paths:
            selected = False
            # Paths are eagerly selected: 
            # Even if only one point is inside of the bounding volume 
            # the path is selected.
            for point in old_path.points:
                if bounding.contains(point):
                    selected = True
            if selected is delete_selected:
                new_geo.add(old_path.clone())
        return new_geo

def delete(shape, position, width, height, scope="points", delete_selected=True):
    """Delete points or paths that lie within the given bounds."""
    bounding = Rect.centeredRect(position.x, position.y, width, height)
    return delete_bounding(shape, bounding, scope, delete_selected)

# TODO distribute

def edit(shape, point_deltas):
    """Edit points non-destructively."""
    if shape is None: return None
    new_shape = shape.clone()
    points = new_shape.points
    deltas = _string_to_dict(point_deltas)
    for index in deltas.keys():
        try:
            dx, dy = deltas[index]
            points[index].x += dx
            points[index].y += dy
        except IndexError:
            pass
    return new_shape
    
def ellipse(position, width, height):
    p = Path()
    p.ellipse(position.x, position.y, width, height)
    return p

def fit(shape, position, width, height, keepProportions):
    """Fit a shape within bounds."""
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

# TODO freehand

def grid(rows, columns, width, height, position):
    """Create a grid of points."""
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

def group(shapes):
    g = Geometry()
    [g.extend(shape) for shape in shapes]
    return g

def import_svg(file_name, centered=False, position=Point.ZERO):
    """Import geometry from a SVG file."""
    # We defer loading the SVG library until we need it.
    # This makes creating a node faster.
    import svg
    if not file_name: return None
    f = file(file_name, 'r')
    s = f.read()
    f.close()
    g = Geometry()
    paths = svg.parse(s, True)
    for path in paths:
        g.add(path)
    t = Transform()
    if centered:
        x, y, w, h = list(g.bounds)
        t.translate(-x-w/2, -y-h/2)
    t.translate(position)
    g = t.map(g)
    return g

def line(point1, point2):
    p = Path()
    p.line(point1.x, point1.y, point2.x, point2.y)
    p.strokeColor = Color.BLACK
    p.strokeWidth = 1
    return p

def line_angle(position, distance, angle):
    p = Path()
    x1, y1 = coordinates(position.x, position.y, distance, angle)
    p.line(position.x, position.y, x1, y1)
    p.strokeColor = Color.BLACK
    p.strokeWidth = 1
    return p

def null(shape):
    """Return the shape as-is."""
    return shape

# TODO place

def polygon(position, radius, sides, align):
    """Draw a polygon."""
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
    return p

def rect(position, width, height):
    """Create a rectangle."""
    p = Path()
    p.rect(position.x, position.y, width, height)
    return p

def rounded_rect(position, width, height, roundness):
    """Create a rounded rectangle."""
    p = Path()
    p.roundedRect(position.x, position.y, width, height, roundness.x, roundness.y)
    return p

def reflect(shape, position, angle, keep_original):
    """Mirrors and copies the geometry across an invisible axis."""
    if shape is None: return None
    g = Geometry()
    shape = shape.clone()
    if keep_original:
        g.extend(shape)
        
    for point in shape.points:
        d = distance(point.x, point.y, position.x, position.y)
        a = angle(point.x, point.y, position.x, position.y)
        x, y = coordinates(position.x, position.y, d * cos(radians(a - position.angle)), 180 + position.angle)
        d = distance(point.x, point.y, x, y)
        a = angle(point.x, point.y, x, y)
        point.x, point.y = coordinates(point.x, point.y, d * 2, a)
        
    g.extend(shape)
    return g

def resample_by_length(shape, length):
    if shape is None: return None
    return shape.resampleByLength(length)

def resample_by_amount(shape, points, per_contour=False):
    return shape.resampleByAmount(points, per_contour)

def resample(shape, method, length, points, per_contour=False):
    if method == 'length':
        return resample_by_length(shape, length)
        return shape.resampleByLength(length)
    else:
        return resample_by_amount(shape, points, per_contour)

def scatter(shape, amount, seed):
    """Generate points within the boundaries of a shape."""
    if shape is None: return None
    seed(seed)
    bx, by, bw, bh = list(shape.bounds)
    points = []
    for i in xrange(amount):
        tries = 100
        while tries > 0:
            pt = Point(bx + uniform(0, 1) * bw, by + uniform(0, 1) * bh)
            if shape.contains(pt):
                points.append(pt)
                break
            tries -= 1
    return points

# TODO shape_on_path

def snap(shape, distance, strength, position=Point.ZERO):
    """Snap geometry to a grid."""
    def _snap(v, offset=0.0, distance=10.0, strength=1.0):
        return (v * (1.0-strength)) + (strength * round(v / distance) * distance)

    if shape is None: return None
    new_shape = shape.clone()
    strength /= 100.0
    for pt in new_shape.points:
       pt.x = snap(pt.x+position.x, position.x, distance, strength) - position.x
       pt.y = snap(pt.y + position.y, position.y, distance, strength)  - position.y
    return new_shape

# TODO sort

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
    return p

def switch(shapes, index=0):
    selected_shape = shapes.get(index, None)
    if selected_shape is not None:
        return [selected_shape]
    else:
        return []

# TODO text_on_path

def textpath(text, font_name="Verdana", font_size=24, align="CENTER", position=Point.ZERO, width=0, height=0):
    """Create a path out of text."""
    t = Text(text, position.x, position.y, width, height)
    t.fontName = font_name
    t.fontSize = font_size
    # valueOf requires a correct value: LEFT, CENTER, RIGHT or JUSTIFY. Anything else will
    # make it crash. If users start doing crazy things and change the alignment, at least
    # make sure you catch the error.
    try:
        t.align = Text.Align.valueOf(align)
    except:
        pass
    return t.path
    
def transform(shape, order, translate, angle, scale):
    """Transforms the location, rotation and scale of a shape."""
    if shape is None: return None
    t = Transform()
    # Each letter of the order describes an operation.
    for op in order:
        if op == 't':
            t.translate(translate.x, translate.y)
        elif op == 'r':
            t.rotate(angle)
        elif op == 's':
            t.scale(scale.x / 100, scale.y / 100)
    # Transform.map clones and transforms the geometry.
    return t.map(shape)

def translate(shape, translate):
    """Move the shape."""
    if shape is None: return None
    return Transform.translated(translate).map(shape)

def scale(shape, scale):
    """Scale the given shape."""
    if shape is None: return None
    return Transform.scaled(scale.x / 100.0, scale.y / 100.0).map(shape)

def rotate(shape, angle):
    """Rotate the given shape."""
    if shape is None: return None
    return Transform.rotated(angle).map(shape)

def wiggle_points(shape, offset, seed=0):
    _seed(seed)
    new_shape = shape.clone()
    for point in new_shape.points:
        dx = (uniform(0, 1) - 0.5) * offset.x * 2
        dy = (uniform(0, 1) - 0.5) * offset.y * 2
        point.x += dx
        point.y += dy
    return new_shape
    
def wiggle_paths(shape, offset, seed=0):
    _seed(seed)
    new_shape = Geometry()
    for path in shape.paths:
        dx = (uniform(0, 1) - 0.5) * offset.x * 2
        dy = (uniform(0, 1) - 0.5) * offset.y * 2
        t = Transform()
        t.translate(dx, dy)
        new_shape.add(t.map(path))
    return new_shape

def wiggle_contours(shape, offset, seed=0):
    _seed(seed)
    new_shape = shape.clone()
    for path in new_shape.paths:
        for contour in path.contours:
            dx = (uniform(0, 1) - 0.5) * offset.x * 2
            dy = (uniform(0, 1) - 0.5) * offset.y * 2
            for point in contour.points:
                point.x += dx
                point.y += dy
    return new_shape
    
def wiggle(shape, scope, offset, seed=0):
    """Shift points by a random amount."""
    if shape is None: return None
    functions = { "points": wiggle_points, 
                  "contours": wiggle_contours, 
                  "paths": wiggle_paths}
    fn = functions.get(scope)
    if fn is None: return None
    return fn(shape, offset, seed)

def makePoint(x, y):
    return Point(x, y)

def makeColor(r, g, b, a, range):
    return Color(r / range, g / range, b / range, a / range)

#### Helper Functions ####

def _string_to_dict(s):
    # Convenience function that converts an input string into
    # a dictionary where the key is the index of the changed point
    # and the value a tuple with the amount the point moves (delta-x, delta-y) 
    d = {}
    for el in s.split("P"):
        if el:
            item = el.strip().split(" ")
            try:
                index = int(item[0])
                dx = float(item[1])
                dy = float(item[2])
                d[index] = (dx, dy)
            except:
                pass
    return d
