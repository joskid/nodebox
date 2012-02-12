from math import pi, sin, cos, radians
from random import seed as _seed, uniform

from java.awt.geom import Arc2D

from nodebox.graphics import Geometry, Path, Contour, Color, Transform, Text, Point, Rect
from nodebox.util.Geometry import coordinates, angle, distance

def generator():
    """Serve as a template for future functions that generate geometry"""
    p = Path()
    p.rect(0, 0, 100, 100)
    return p

def filter(shape):
    """Serve as a template for future functions that filter geometry"""
    if shape is None: return None
    t = Transform()
    t.rotate(45)
    return t.map(shape)

def align(shape, position, halign="center", valign="middle"):
    """Align a shape in relation to the origin."""
    if shape is None: return None
    x, y = position.x, position.y
    bounds = shape.bounds
    if halign == "left":
        dx = x - bounds.x
    elif halign == "right":
        dx = x - bounds.x - bounds.width
    elif halign == "center":
        dx = x - bounds.x - bounds.width / 2
    else:
        dx = 0
    if valign == "top":
        dy = y - bounds.y
    elif valign == "bottom":
        dy = y - bounds.y - bounds.height
    elif valign == "middle":
        dy = y - bounds.y - bounds.height / 2
    else:
        dy = 0
        
    t = Transform()
    t.translate(dx, dy)
    return t.map(shape)

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
    new_shape.fillColor = fill
    if strokeWidth > 0:
        new_shape.strokeColor = stroke
        new_shape.strokeWidth = strokeWidth
    else:
        new_shape.strokeColor = None
    return new_shape

def _flatten(geo):
    compound = Path()
    first = True
    for path in geo.paths:
        if first:
             compound = path
             first = False
        else:
             compound = compound.united(path)
    return compound

def _flatten_to_paths(fn):
    def _function(shape1, shape2, *args, **kwargs):
        if isinstance(shape1, Geometry):
            shape1 = _flatten(shape1)
        if isinstance(shape2, Geometry):
            shape2 = _flatten(shape2)
        return fn(shape1, shape2, *args, **kwargs)
    return _function

@_flatten_to_paths
def compound(shape1, shape2, function="united", invert_difference=False):
    """Add, subtract or intersect geometry."""
    if shape1 is None: return None
    if shape2 is None: return shape1.clone()
    # The invert turns the operation around.
    if invert_difference:
        shape1, shape2 = shape2, shape1

    # Combine the two compound paths using the given function.
    if function == "united":
        return shape1.united(shape2)
    elif function == "subtracted":
        return shape1.subtracted(shape2)
    elif function == "intersected":
        return shape1.intersected(shape2)
    return None

def _map_list_to_points(fn):
    from java.lang import Iterable
    
    def _function(_list, *args, **kwargs):
        if isinstance(_list, (Path, Geometry, Contour)):
            return fn(_list.points, *args, **kwargs)
        elif isinstance(_list, (list, tuple, Iterable)):
            if len(_list) == 0: return None
            first = _list[0]
            if isinstance(first, Point):
                return fn(_list, *args, **kwargs)
            elif isinstance(first, (Path, Geometry, Contour)):
                if len(_list) == 1:
                    return fn(first.points, *args, **kwargs)
                else:
                    g = Geometry()
                    for el in _list:
                        g.add(fn(el.points, *args, **kwargs))
                    return g
        return None
    return _function

@_map_list_to_points
def connect(points, closed=True):
    """Connects all points in a path."""
    
    if not points: return None
    if len(points) < 2: return None
    points = list(points)
    start = points[0]
    p = Path()
    p.moveto(start.x, start.y)
    for point in points[1:]:
        p.lineto(point.x, point.y)
    if closed:
        p.close()
    p.stroke = Color.BLACK
    p.strokeWidth = 1.0
    return p

def copy(shape, copies, transform_order='tsr', translate=Point.ZERO, rotate=0, scale=Point.ZERO):
    """Create multiple copies of a shape."""
    if shape is None: return None
    if isinstance(shape, Path):
        shape = shape.asGeometry()
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
        g.extend(t.map(shape))
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

def _map_geo_to_paths(fn):
    def _function(shape, *args, **kwargs):
        if isinstance(shape, Path):
            return fn(shape, *args, **kwargs)
        elif isinstance(shape, Geometry):
            g = Geometry()
            for path in shape.paths:
                result = fn(path, *args, **kwargs)
                if isinstance(result, Path):
                    g.add(result)
                elif isinstance(result, Geometry):
                    g.extend(result)
            return g
        return None
    return _function

def _map_paths_to_geo(fn):
    def _function(shape, *args, **kwargs):
        if isinstance(shape, Path):
            shape = shape.asGeometry()
        if isinstance(shape, Geometry):
            return fn(shape, *args, **kwargs)
        return None
    return _function

@_map_geo_to_paths
def delete_points(path, bounding, delete_selected=True):
    if path is None or bounding is None: return None
    new_path = Path(path, False) # cloneContours = False
    for old_contour in path.contours:
        new_contour = Contour()
        for point in old_contour.points:
            if bounding.contains(point) == delete_selected:
                new_contour.addPoint(Point(point.x, point.y, point.type))
        new_contour.closed = old_contour.closed
        new_path.add(new_contour)
    return new_path

@_map_paths_to_geo
def delete_paths(geo, bounding, delete_selected=True):
    if geo is None or bounding is None: return None
    new_geo = Geometry()
    for old_path in geo.paths:
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
    
def delete_bounding(shape, bounding, scope="points", delete_selected=True):
    if shape is None or bounding is None: return None
    if scope == "points": return delete_points(shape, bounding, delete_selected)
    if scope == "paths": return delete_paths(shape, bounding, delete_selected)

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

def fit(shape, position, width, height, keep_proportions):
    """Fit a shape within bounds."""
    if shape is None: return None

    px, py, pw, ph = list(shape.bounds)

    # Make sure pw and ph aren't infinitely small numbers.
    # This will lead to incorrect transformations with for examples lines.
    if 0 < pw <= 0.000000000001: pw = 0
    if 0 < ph <= 0.000000000001: ph = 0

    t = Transform()
    t.translate(position.x, position.y)
    if keep_proportions:
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
        column_size = left = position.x
    if rows > 1:
        row_size = height / (rows - 1)
        top = position.y - height / 2
    else:
        row_size = top = position.y
        
    points = []
    for ri in xrange(rows):
        for ci in xrange(columns):
            x = left + ci * column_size
            y = top + ri * row_size
            points.append(Point(x, y))
    return points

def to_points(shape):
    if shape is None: return None
    return shape.points

def group(shapes):
    if not shapes: return None
    g = Geometry()
    for shape in shapes:
        if isinstance(shape, Geometry):
            g.extend(shape)
        elif isinstance(shape, Path):
            g.add(shape)
        else:
            raise "Unable to group %ss. I can only group paths or geometry objects."  % shape
    return g

def ungroup(shape):
    if shape is None: return None
    if not isinstance(shape, Geometry): return shape
    return shape.paths

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

def line_angle(position, angle, distance):
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

@_map_geo_to_paths
def reflect(shape, position, _angle, keep_original):
    """Mirrors and copies the geometry across an invisible axis."""
    if shape is None: return None
    
    new_shape = shape.cloneAndClear()
    for contour in shape.contours:
        c = Contour()
        for point in contour.points:  
            d = distance(point.x, point.y, position.x, position.y)
            a = angle(point.x, point.y, position.x, position.y)
            x, y = coordinates(position.x, position.y, d * cos(radians(a - _angle)), 180 + _angle)
            d = distance(point.x, point.y, x, y)
            a = angle(point.x, point.y, x, y)
            px, py = coordinates(point.x, point.y, d * 2, a)
            c.addPoint(Point(px, py, point.type))
        if contour.closed:
            c.close()
        new_shape.add(c)
        
    if keep_original:
        g = Geometry()
        g.add(shape)
        g.add(new_shape)
        return g
        
    return new_shape

def resample_by_length(shape, length):
    if shape is None: return None
    return shape.resampleByLength(length)

def resample_by_amount(shape, points, per_contour=False):
    if shape is None: return None
    return shape.resampleByAmount(points, per_contour)

def resample(shape, method, length, points, per_contour=False):
    if shape is None: return None
    if method == 'length':
        return resample_by_length(shape, length)
    else:
        return resample_by_amount(shape, points, per_contour)

def scatter(shape, amount, seed):
    """Generate points within the boundaries of a shape."""
    if shape is None: return None
    _seed(seed)
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

def shape_on_path(shape, template, amount, dist, start, keep_geometry):
    if shape is None: return None
    if template is None: return None
    
    if isinstance(shape, Path):
        shape = shape.asGeometry()
    if isinstance(template, Path):
        template = template.asGeometry()
        
    g = Geometry()

    if keep_geometry:
        g.extend(template.clone())
           
    first = True  
    for i in range(amount):
        if first:
            t = start / 100
            first = False
        else:
            t += dist / 500.0
        pt1 = template.pointAt(t)
        pt2 = template.pointAt(t + 0.00001)
        a = angle(pt2.x, pt2.y, pt1.x, pt1.y)
        tp = Transform()
        tp.translate(pt1.x, pt1.y)
        tp.rotate(a - 180)
        new_shape = tp.map(shape)
        g.extend(new_shape)
    return g

@_map_geo_to_paths
def snap(shape, distance, strength, position=Point.ZERO):
    """Snap geometry to a grid."""
    def _snap(v, offset=0.0, distance=10.0, strength=1.0):
        return (v * (1.0-strength)) + (strength * round(v / distance) * distance)

    if shape is None: return None
    new_shape = shape.cloneAndClear()
    strength /= 100.0
    for contour in shape.contours:
        c = Contour()
        for pt in contour.points:
            x = _snap(pt.x + position.x, position.x, distance, strength) - position.x
            y = _snap(pt.y + position.y, position.y, distance, strength) - position.y
            c.addPoint(Point(x, y, pt.type))
        c.closed = contour.closed
        new_shape.add(c)
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
    if not shapes: return None
    if index == 0: return shapes[0]
    return shapes[index % len(shapes)]

def textwidth(text, font_metrics):
    if not text:
        return 0.0
    elif len(text) == 1:
        return float(font_metrics.charWidth(text))
    else:
        return float(font_metrics.stringWidth(text))

def get_font_metrics(font_name, font_size):
    from java.awt.image import BufferedImage
    from java.awt import Font
    tmp_img = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    g = tmp_img.createGraphics()
    return g.getFontMetrics((Font(font_name, Font.PLAIN, int(font_size))))

def text_on_path(shape, text, font_name="Verdana", font_size=20, start=0, loop=True, dy=2.0, keep_geometry=True):
    if shape is None: return None
    if not text: return None
    
    if isinstance(shape, Path):
        shape = shape.asGeometry()
    
    g = Geometry()

    if keep_geometry:
        g.extend(shape.clone())
    
    fm = get_font_metrics(font_name, font_size)
    string_width = textwidth(text, fm)
    dw = string_width / shape.length
    
    first = True
    for i, char in enumerate(text):
        char_width = textwidth(char, fm)
        
        if first:
            t = start / 100.0
            first = False
        else:
            t += char_width / string_width * dw
        
        if loop:    
            t = t % 1.0
            
        pt1 = shape.pointAt(t)
        pt2 = shape.pointAt(t + 0.001)
        a = angle(pt2.x, pt2.y, pt1.x, pt1.y)
        
        tp = Text(char, -char_width, -dy)
        tp.align = Text.Align.LEFT
        tp.fontName = font_name
        tp.fontSize = font_size
        tp.translate(pt1.x, pt1.y)
        tp.rotate(a - 180)
        
        g.add(tp.path)
    
    return g
    
def textpath(text, font_name="Verdana", font_size=24, align="CENTER", position=Point.ZERO, width=0, height=0):
    """Create a path out of text."""
    t = Text(unicode(text), position.x, position.y, width, height)
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
    
def transform(shape, order, translate, rotate, scale):
    """Transforms the location, rotation and scale of a shape."""
    if shape is None: return None
    t = Transform()
    # Each letter of the order describes an operation.
    for op in order:
        if op == 't':
            t.translate(translate.x, translate.y)
        elif op == 'r':
            t.rotate(rotate)
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

def _with_seed(fn):
    def decorated_function(*args, **kwargs):
        seed = args[-1]
        _seed(seed)
        new_args = args[:-1]
        return fn(*new_args, **kwargs)
    return decorated_function

def _map_points(fn):
    def _function(shape, *args, **kwargs):
        if isinstance(shape, list):
            return fn(shape, *args, **kwargs)
        elif isinstance(shape, Point):
            return fn([shape], *args, **kwargs)[0]
        elif isinstance(shape, Contour):
            new_points = fn(shape.points, *args, **kwargs)
            return Contour(new_points, shape.closed)
        elif isinstance(shape, Path):
            path = shape.cloneAndClear()
            for contour in shape.contours:
                path.add(_function(contour, *args, **kwargs))
            return path
        elif isinstance(shape, Geometry):
            g = Geometry()
            for p in shape.paths:
                g.add(_function(p, *args, **kwargs))
            return g
    return _function
    
def _map_contours(fn):
    def _function(shape, *args, **kwargs):
        if isinstance(shape, list):
            return fn(shape, *args, **kwargs)
        elif isinstance(shape, Contour):
            return fn([shape], *args, **kwargs)[0]
        elif isinstance(shape, Path):
            path = shape.cloneAndClear()
            for contour in fn(shape.contours, *args, **kwargs):
                path.add(contour)
            return path
        elif isinstance(shape, Geometry):
            g = Geometry()
            for p in shape.paths:
                g.add(_function(p, *args, **kwargs))
            return g
    return _function

def _map_paths(fn):
    def _function(shape, *args, **kwargs):
        if isinstance(shape, list):
            return fn(shape, *args, **kwargs)
        elif isinstance(shape, Path):
            return fn([shape], *args, **kwargs)[0]
        elif isinstance(shape, Geometry):
            g = Geometry()
            for path in fn(shape.paths, *args, **kwargs):
                g.add(path)
            return g
    return _function

# todo: improve seed argument detection 
@_with_seed
@_map_points
def wiggle_points(points, offset):
    new_points = []
    for point in points:
        dx = (uniform(0, 1) - 0.5) * offset.x * 2
        dy = (uniform(0, 1) - 0.5) * offset.y * 2
        new_points.append(Point(point.x + dx, point.y + dy, point.type))
    return new_points
    
@_with_seed
@_map_paths
def wiggle_paths(paths, offset):
    new_paths = []
    for path in paths:
        dx = (uniform(0, 1) - 0.5) * offset.x * 2
        dy = (uniform(0, 1) - 0.5) * offset.y * 2
        t = Transform()
        t.translate(dx, dy)
        new_paths.append(t.map(path))
    return new_paths

@_with_seed
@_map_contours
def wiggle_contours(contours, offset):
    new_contours = []
    for contour in contours:
        dx = (uniform(0, 1) - 0.5) * offset.x * 2
        dy = (uniform(0, 1) - 0.5) * offset.y * 2
        t = Transform()
        t.translate(dx, dy)
        new_contours.append(Contour(t.map(contour.points), contour.closed))
    return new_contours
    
def wiggle(shape, scope, offset, seed=0):
    """Shift points/contours/paths by a random amount."""
    if shape is None: return None
    functions = { "points": wiggle_points, 
                  "contours": wiggle_contours, 
                  "paths": wiggle_paths}
    fn = functions.get(scope)
    if fn is None: return None
    return fn(shape, offset, seed)

def make_point(x, y):
    return Point(x, y)

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
