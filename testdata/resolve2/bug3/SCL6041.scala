object SCL6041 {
  class A[A, B]
  type Z[T] = A[T, T]

  val z: Z[Int] = new /*resolved: True*/Z[Int]
}