package net.paoloambrosio.drizzle.http

trait HttpResponse {
  def status: Integer
  def body: String
}
