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
