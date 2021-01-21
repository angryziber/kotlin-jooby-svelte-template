interface Array<T> {
  first(): T
  last(): T
  sum(by?: (e: T) => number): number
  max(): T
  max<R>(by: (e: T) => R): R
  min(): T
  min<R>(by: (e: T) => R): R
  indexBy<R>(by: (e: T) => string|number): {[by: string]: T}
  indexBy<R>(by: (e: T) => string|number, combiner: (r, t: T) => R, initial?: R): {[by: string]: R}
  groupBy(by: (e: T) => string|number): {[by: string]: T[]}
  countBy<R>(by: (e: T) => string|number): {[by: string]: number}
}
