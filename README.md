# Silhouette Authentication with ReactiveMongo and Play 2 

This application is based on Silhouette seed Template: 

https://github.com/mohiva/play-silhouette-seed

The original template uses an array on memory to authenticate users, to use a MongoDB database instead, follow the step by step example.

Edit build.sbt and add the following

```scala
libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.5.play24"
)
```

Add ReactiveMongo configuration to application.conf

```scala
mongodb {
  db = "silhouette"
  servers = [ "localhost:27017" ]
}

mongo-async-driver {
  akka {
    loglevel = DEBUG
  }
}
```

Add a DB dependency by editing SilhouetteModule.scala

```scala

import reactivemongo.api._

def configure() {
  bind[DB].toInstance {
    import com.typesafe.config.ConfigFactory
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.collection.JavaConversions._

    val config = ConfigFactory.load
    val driver = new MongoDriver
    val connection = driver.connection(
      config.getStringList("mongodb.servers"),
      MongoConnectionOptions(),
      Seq()
    )
    connection.db(config.getString("mongodb.db"))
  }
}
```
Inject DB to UserDAOImpl.scala and PasswordInfoDAO.scala

```scala

import javax.inject.Inject
import reactivemongo.api._

class UserDAOImpl @Inject() (db : DB) extends UserDAO {
	
}

class PasswordInfoDAO @Inject() (db : DB) extends DelegableAuthInfoDAO[PasswordInfo] {
	
}

```
And finally rewrite functions on the both classes, here I will just show UserDaoImpl functions

```scala

class UserDAOImpl @Inject() (db : DB) extends UserDAO {

  def collection: JSONCollection = db.collection[JSONCollection]("user")

  def find(loginInfo: LoginInfo) : Future[Option[User]] = {
    collection.find(Json.obj( "loginInfo" -> loginInfo )).one[User]
  }

  def find(userID: UUID) : Future[Option[User]] = {
    collection.find(Json.obj("userID" -> userID)).one[User]
  }

  def save(user: User) = {
    collection.insert(user)
    Future.successful(user)
  }
}

```



