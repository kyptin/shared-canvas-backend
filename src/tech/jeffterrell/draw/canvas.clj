(ns tech.jeffterrell.draw.canvas
  (:require [clojure.spec.alpha :as s])
  (:import javax.imageio.ImageIO
           [java.io ByteArrayInputStream ByteArrayOutputStream]
           [java.awt Color Rectangle]
           [java.awt.image BufferedImage]))

(def width 800)
(def height 600)

(defn new-canvas []
  (BufferedImage. width height BufferedImage/TYPE_INT_RGB))

(defn draw-rect
  [^BufferedImage canvas x y width height red green blue]
  (doto (.createGraphics canvas)
    (.setPaint (Color. (float red) (float green) (float blue)))
    (.draw (Rectangle. x y width height))))

(defn canvas-as-png-data
  [canvas]
  (let [buffer (ByteArrayOutputStream.)]
    (ImageIO/write canvas "PNG" buffer)
    (ByteArrayInputStream. (.toByteArray buffer))))
