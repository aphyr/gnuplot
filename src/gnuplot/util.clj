(ns gnuplot.util
  "The great dust-filled 'MISC' drawer in the tool shed of life"
  (:import gnuplot.InputStream
           gnuplot.InputStream$Streamable))

(def ^String encoding "UTF-8")

(defprotocol IStringSeqStreamable
  (flip [this] "Unsynchronized. Consumes an element of the seq and loads it
               into the internal buffer. Returns true if there was another
               element, or false if the stream is now empty."))

(deftype StringSeqStreamable [^:volatile-mutable ^bytes buffer
                              ^:volatile-mutable ^long offset
                              ^:volatile-mutable s]
  IStringSeqStreamable
  (flip [this]
;    (prn :flip :buffer (alength buffer) :offset offset :s (first s))
    (if (nil? s)
      (do (set! buffer (byte-array 0))
          (set! offset 0)
          false)
      (do (set! buffer (.getBytes ^String (first s) encoding))
          (set! offset 0)
          (set! s (next s))
          true)))

  InputStream$Streamable
  (available [this]
    (locking this
      (- (alength buffer) offset)))

  (close [this]
    (locking this
      (set! buffer (byte-array 0))
      (set! offset 0)
      (set! s nil)))

  (skip [this n]
    (locking this
      (loop [remaining n]
        (let [bufsize (alength buffer)]
          (if (< (+ offset remaining) bufsize)
            ; We're within the buffer
            (do (set! offset (+ offset remaining))
                n)

            ; We're gonna run over the buffer; flip!
            (if (flip this)
              ; There's more
              (recur (- remaining bufsize))
              ; All gone
              (- remaining bufsize)))))))

  (read [this]
    (locking this
      (loop []
        (if (< offset (alength buffer))
          ; We have a buffered byte
          (let [value (bit-and (aget buffer offset) 0xFF)]
            (set! offset (inc offset))
            value)

          ; Buffer's consumed! Time to move on.
          (if (flip this)
            (recur)
            -1)))))

  (read [this dest dest-offset length]
    (locking this
      (if (and (zero? (.available this))
               (nil? s))
        ; End of stream
        -1

        (loop [dest-offset  dest-offset
               length'      length]
          (let [bufsize (alength buffer)]
            (if (< (+ offset length') bufsize)
              ; We can satisfy this request from the buffer.
              (do ; (prn :partial-copy length' :of bufsize :from offset :to dest-offset)
                  (System/arraycopy buffer offset dest dest-offset length')
                  (set! offset (+ offset length'))
                  length)

              ; Consume remainder of buffer
              (let [consumed (- bufsize offset)]
                ; (prn :copy-remaining consumed :from offset :to dest-offset)
                (System/arraycopy buffer offset dest dest-offset consumed)
                (if (flip this)
                  (do ; (prn :flipped)
                      ; Keep going
                      (recur (+ dest-offset consumed)
                             (- length'     consumed)))
                  ; Empty
                  (- length (- length' consumed)))))))))))

(defn strings->input-stream
  "Wraps a sequence of strings as an inputstream."
  [s]
  (InputStream. (StringSeqStreamable. (byte-array 0) 0 (seq s))))
