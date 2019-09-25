(ns tech.jeffterrell.draw.canvas
  "This namespace contains the creation and manipulation functions for the
  stateful canvas object used in this simple backend demo."
  (:require [clojure.spec.alpha :as s])
  (:import javax.imageio.ImageIO
           [java.io ByteArrayInputStream ByteArrayOutputStream]
           [java.awt Color Rectangle]
           [java.awt.image BufferedImage]))

(def width 800)
(def height 600)

(defn new-canvas
  "Return a new stateful canvas object with a hardcoded width and height."
  []
  (BufferedImage. width height BufferedImage/TYPE_INT_RGB))

(defn draw-rect
  "Draw a rectangle of the given dimensions and color on the given canvas.
  Returns nil, so call it as a statement rather than a function."
  [^BufferedImage canvas rect]
  (let [{:keys [x y width height color]} rect
        {:keys [red green blue]} color]
    (doto (.createGraphics canvas)
      (.setPaint (Color. (float red) (float green) (float blue)))
      (.draw (Rectangle. x y width height)))))

(defn canvas-as-png-data
  "Return the canvas as PNG data, ready to be served as the body of a response
  with a content-type of image/png."
  [canvas]
  (let [buffer (ByteArrayOutputStream.)]
    (ImageIO/write canvas "PNG" buffer)
    (ByteArrayInputStream. (.toByteArray buffer))))

(defn clear
  "Reset the canvas to a blank slate."
  [^BufferedImage canvas]
  (doto (.createGraphics canvas)
    (.setBackground (Color. 0 0 0 255))
    (.clearRect 0 0 (.getWidth canvas) (.getHeight canvas))))
