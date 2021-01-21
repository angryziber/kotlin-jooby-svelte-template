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

Array.prototype.indexBy = function(by, combiner = (r, e) => e, initial?) {return this.reduce((r, e) => {
  const v = by(e)
  r[v] = combiner(r[v] ?? initial, e)
  return r
}, {})}

Array.prototype.groupBy = function(by) {
  return this.indexBy(by, (r, e) => r.concat(e), [])
}

Array.prototype.countBy = function(by) {
  return this.indexBy(by, r => r + 1, 0)
}
