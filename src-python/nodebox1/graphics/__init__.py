from java.lang import Boolean
from nodebox.graphics import CanvasContext, Path, NodeBoxError

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
        self._setAttributesFromKwargs(p, **kwargs)
        return p
    oval = ellipse
    
    def line(self, x1, y1, x2, y2, draw=True, **kwargs):
        p = CanvasContext.line(self, x1, y1, x2, y2, Boolean(draw))
        self._setAttributesFromKwargs(p, **kwargs)
        return p
    
    def star(self, startx, starty, points=20, outer=100, inner=50, draw=True, **kwargs):
        p = CanvasContext.star(self, startx, starty, points, outer, inner, Boolean(draw))
        self._setAttributesFromKwargs(p, **kwargs)
        return p
    
    def arrow(self, x, y, width=100, type=CanvasContext.NORMAL, draw=True, **kwargs):
        p = CanvasContext.arrow(self, x, y, width, type, Boolean(draw))
        self._setAttributesFromKwargs(p, **kwargs)
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
        self._setAttributesFromKwargs(p, **kwargs)
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
    
    ### Color Commands ###

    def colormode(self, mode=None, range=None):
        if mode is not None:
            return CanvasContext.colormode(self, mode)
        # todo: range
        return CanvasContext.colormode(self)

    # todo: colorrange

    def strokewidth(self, width=None):
        if width is not None:
            return CanvasContext.strokewidth(self, width)
        return CanvasContext.strokewidth(self)
    
    ### Font Commands ###
    
    def font(self, fontname=None, fontsize=None):
        if fontname is not None and fontsize is not None:
            return CanvasContext.font(self, fontname, fontsize)
        elif fontname is not None:
            return CanvasContext.font(self, fontname)
        elif fontsize is not None:
            CanvasContext.fontsize(self, fontsize)
        return CanvasContext.font(self)
    
    def fontsize(self, fontsize=None):
        if fontsize is not None:
            return CanvasContext.fontsize(self, fontsize)
        return CanvasContext.fontsize(self)
            
    def lineheight(self, lineheight=None):
        if lineheight is not None:
            return CanvasContext.lineheight(self, lineheight)
        return CanvasContext.lineheight(self)

    def align(self, align=None):
        if align is not None:
            return CanvasContext.align(self, align)
        return CanvasContext.align(self)
    
    def text(self, txt, x, y, width=0, height=0, outline=False, draw=True, **kwargs):
        if outline:
            t = CanvasContext.text(self, txt, x, y, width, height, Boolean(False))
            p = t.path
            self._setAttributesFromKwargs(p, **kwargs)
            if draw:
                self.addPath(p)
            return p
        else:
            t = CanvasContext.text(self, txt, x, y, width, height, Boolean(draw))
            # todo: handle kwargs
            return t
            
    def textpath(self, txt, x, y, width=None, height=None, **kwargs):
        if width is None: width = 0
        if height is None: height = 0
        p = CanvasContext.textpath(self, txt, x, y, width, height)
        self._setAttributesFromKwargs(p, **kwargs)
        return p

    def textmetrics(self, txt, width=None, height=None, **kwargs):
        if width is None: width = 0
        if height is None: height = 0
        r = CanvasContext.textmetrics(self, txt, width, height)
        # todo: handle kwargs?
        return r

    def textwidth(self, txt, width=None, **kwargs):
        if width is None: width = 0
        w = CanvasContext.textwidth(self, txt, width)
        # todo: handle kwargs?
        return w

    def textheight(self, txt, height=None, **kwargs):
        if height is None: height = 0
        h = CanvasContext.textheight(self, txt, height)
        # todo: handle kwargs?
        return h
    
    def _setAttributesFromKwargs(self, item, **kwargs):
        keys = kwargs.keys()
        if isinstance(item, Path):
            for kwarg, attr in (('fill', 'fillColor'), ('stroke', 'strokeColor'), ('strokewidth', 'strokeWidth')):
                if kwarg in keys:
                    v = kwargs.pop(kwarg)
                    setattr(item, attr, v)
        remaining = kwargs.keys()
        if remaining:
            raise NodeBoxError, "Unknown argument(s) '%s'" % ", ".join(remaining)