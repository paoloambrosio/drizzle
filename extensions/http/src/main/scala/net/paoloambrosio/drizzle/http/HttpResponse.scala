package net.paoloambrosio.drizzle.http

trait HttpResponse {
  def status: Int
  def body: String
}
