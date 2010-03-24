from java.lang import Boolean
from nodebox.graphics import CanvasContext

class Context(CanvasContext):
    def __init__(self, canvas=None, ns=None):
        args = canvas is not None and [canvas] or []
        CanvasContext.__init__(self, *args)
        if ns is None:
            ns = {}
        self._ns = ns
        
    def size(self, width, height):
        CanvasContext.size(self, width, height)
        # To keep the WIDTH and HEIGHT properties up to date in the executing script,
        # the Context object must have access to its namespace. Normally, we passed this
        # during construction time.
        self._ns["WIDTH"] = width
        self._ns["HEIGHT"] = height

    #### Primitives ####
    
    # todo: rect
    
    def ellipse(self, x, y, width, height, draw=True, **kwargs):
        p = CanvasContext.ellipse(self, x, y, width, height, Boolean(draw))
        # todo: handle kwargs
        return p
    oval = ellipse
    
    def line(self, x1, y1, x2, y2, draw=True, **kwargs):
        p = CanvasContext.line(self, x1, y1, x2, y2, Boolean(draw))
        # todo: handle kwargs
        return p
    
    def star(self, startx, starty, points=20, outer=100, inner=50, draw=True, **kwargs):
        p = CanvasContext.star(self, startx, starty, points, outer, inner, Boolean(draw))
        # todo: handle kwargs
        return p
    
    def arrow(self, x, y, width=100, type=CanvasContext.NORMAL, draw=True, **kwargs):
        p = CanvasContext.arrow(self, x, y, width, type, Boolean(draw))
        # todo: handle kwargs, implement arrow45
        return p
        
    ### Path Commands ###

    def beginpath(self, x=None, y=None):
        if x != None and y != None:
            CanvasContext.beginpath(self, x, y)
        else:
            CanvasContext.beginpath(self)
    
    def endpath(self, draw=True):
        return CanvasContext.endpath(self, Boolean(draw))
    
    def drawpath(self, path, **kwargs):
        p = CanvasContext.drawpath(self, path)
        # todo: handle kwargs
        return p
    
    def autoclosepath(self, **kwargs):
        close = kwargs.get("close")
        if close is None:
            return CanvasContext.autoclosepath(self)
        else:
            return CanvasContext.autoclosepath(self, Boolean(close))
    
    # todo: findpath
    
    ### Transformation commands ###
    
    def transform(self, mode=None):
        if mode is None:
            return CanvasContext.transform(self)
        else:
            return CanvasContext.transform(self, mode)
    
    def translate(self, tx=0, ty=0):
        CanvasContext.translate(self, tx, ty)
    
    def rotate(self, degrees=0, radians=0):
        # todo: radians
        CanvasContext.rotate(self, degrees)
    
    def scale(self, sx=1, sy=None):
        if sy is None:
            CanvasContext.scale(self, sx)
        else:
            CanvasContext.scale(self, sx, sy)
    
    def skew(self, kx=0, ky=None):
        if ky is None:
            CanvasContext.skew(self, kx)
        else:
            CanvasContext.skew(self, kx, ky)
