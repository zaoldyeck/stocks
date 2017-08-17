import java.util.Date

import play.api.libs.json.{JsArray, JsValue, Json, Reads}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.ahc.AhcCurlRequestLogger

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class FinanceFetcher {
  // http://www.twse.com.tw/exchangeReport/STOCK_DAY?response=json&date=20170801&stockNo=2330
  def getRealTimePrice(id: String): Future[Double] = {
    Http.client.url(s"http://mis.twse.com.tw/stock/fibest.jsp?stock=$id").get.flatMap {
      response =>
        Http.client.url(s"http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_$id.tw&json=1&delay=0&_=${new Date().getTime}")
          .addCookies(response.cookies: _*)
          .get
    } map {
      response =>
        (response.body[JsValue].apply("msgArray")(0) \ "z").as[String].toDouble
    }
  }

  def getHistoryPrice(id: String, year: Int, month: Int): Future[List[HistoryPrice]] = {
    val monthString: String = if (month < 10) "0" + month else month.toString
    Http.client.url(s"http://www.twse.com.tw/en/exchangeReport/STOCK_DAY?response=json&date=$year${monthString}01&stockNo=$id").get.map {
      response =>
        val stringses: immutable.Seq[List[String]] = response.body[JsValue].apply("data").as[List[List[String]]]
        //stringses.map(HistoryPrice(_:*))
    }
  }

  //  def getFinanceReport(id: String, fromYear: Int, fromSeason: Int, toYear: Int, toSeason: Int): Future[FinanceReport] = {
  //    Http.client.url(s"https://statementdog.com/api/v1/fundamentals/$id/$fromYear/$fromSeason/$toYear/$toSeason").get.map {
  //
  //    }
  //  }

  case class FinanceReport(meanPER: Double, meanROA: Double)

  case class HistoryPrice(date: Date,
                          tradeVolume: Int,
                          tradeValue: Double,
                          openingPrice: Double,
                          highestPrice: Double,
                          lowestPrice: Double,
                          closingPrice: Double,
                          change: Double,
                          transaction: Int)

  implicit val historyPriceReads: Reads[HistoryPrice] = Json.reads[HistoryPrice]
}
