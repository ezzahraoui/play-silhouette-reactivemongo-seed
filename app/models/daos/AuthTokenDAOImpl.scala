package models.daos

import java.util.UUID
import javax.inject._
import models.AuthToken
import org.joda.time.DateTime
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import reactivemongo.api._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo._
import reactivemongo.play.json.collection.JSONCollection



/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDAOImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends AuthTokenDAO {

  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("silhouette.token"))

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthToken]] = {
    val query = Json.obj("id" -> id)
    collection.flatMap(_.find(query).one[AuthToken])
  }

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: DateTime): Future[Seq[AuthToken]] = {
    val query = Json.obj("expiry" -> Json.obj("$lt" -> dateTime))
    collection.flatMap(_.find(query).cursor[AuthToken](readPreference = ReadPreference.primary).collect[Seq]())
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] = {
    collection.flatMap(_.insert(token))
    Future.successful(token)
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Unit] = {
    val query = Json.obj("id" -> id)
    collection.flatMap(_.remove(query))
    Future.successful(())
  }
}
