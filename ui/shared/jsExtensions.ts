// Some convenient extension functions
interface Array<T> {
  first(): T|undefined
  last(): T|undefined
  sum(by?: (T) => number): number
  max<R>(by?: (T) => R): R
  min<R>(by?: (T) => R): R
  groupBy<R>(by: (T) => string|number, combiner: (r, t: T) => R, initial?: R): {[by: string]: R}
}

Array.prototype.first = function() {return this[0]}
Array.prototype.last = function() {return this[this.length - 1]}
Array.prototype.sum = function(by = v => v) {return this.reduce((r, e) => r + by(e), 0)}

Array.prototype.max = function(by = v => v) {return this.reduce((r, e) => {
  const v = by(e)
  return v > r ? v : r
}, this.length ? by(this.first()) : undefined)}

Array.prototype.min = function(by = v => v) {return this.reduce((r, e) => {
  const v = by(e)
  return v < r ? v : r
}, this.length ? by(this.first()) : undefined)}

Array.prototype.groupBy = function(by, combiner, initial) {return this.reduce((r, e) => {
  const v = by(e)
  r[v] = combiner(r[v] ?? initial, e)
  return r
}, {})}
