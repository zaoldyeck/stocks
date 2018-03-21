import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class StockFetcherTest extends FunSuite {
  private val stockFetcher = new StockFetcher()

  test("getAllStocks") {
    stockFetcher.getAllStocks.map {
      stocks => assert(stocks.nonEmpty)
    }
  }
}