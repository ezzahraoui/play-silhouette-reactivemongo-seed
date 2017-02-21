# Silhouette Authentication with ReactiveMongo and Play 2 

This application is based on Silhouette seed Template: 

https://github.com/mohiva/play-silhouette-seed

And Silhouette ReactiveMongo persistence layer:

https://github.com/mohiva/play-silhouette-persistence-reactivemongo

The original template uses an array on memory to authenticate users, to use a MongoDB database instead, follow the step by step example.

Edit build.sbt and add the following dependencies

```scala
libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.1",
  "com.mohiva" %% "play-silhouette-persistence-reactivemongo" % "4.0.1"
)
```

Add ReactiveMongo configuration to application.conf

```scala
play.modules.enabled += "play.modules.reactivemongo.ReactiveMongoModule"
mongodb.uri = "mongodb://localhost:27017/test"
```

Add the implementation of the different delegables on SilhouetteModule.scala

```scala
/**
 * Provides the implementation of the delegable OAuth1 auth info DAO.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param config The Play configuration.
 * @return The implementation of the delegable OAuth1 auth info DAO.
 */
@Provides
def provideOAuth1InfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OAuth1Info] = {
  implicit lazy val format = Json.format[OAuth1Info]
  new MongoAuthInfoDAO[OAuth1Info](reactiveMongoApi, config)
}

/**
 * Provides the implementation of the delegable OAuth2 auth info DAO.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param config The Play configuration.
 * @return The implementation of the delegable OAuth2 auth info DAO.
 */
@Provides
def provideOAuth2InfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OAuth2Info] = {
  implicit lazy val format = Json.format[OAuth2Info]
  new MongoAuthInfoDAO[OAuth2Info](reactiveMongoApi, config)
}

/**
 * Provides the implementation of the delegable OpenID auth info DAO.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param config The Play configuration.
 * @return The implementation of the delegable OpenID auth info DAO.
 */
@Provides
def provideOpenIDInfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OpenIDInfo] = {
  implicit lazy val format = Json.format[OpenIDInfo]
  new MongoAuthInfoDAO[OpenIDInfo](reactiveMongoApi, config)
}

/**
 * Provides the implementation of the delegable password auth info DAO.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param config The Play configuration.
 * @return The implementation of the delegable password auth info DAO.
 */
@Provides
def providePasswordInfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[PasswordInfo] = {
  implicit lazy val format = Json.format[PasswordInfo]
  new MongoAuthInfoDAO[PasswordInfo](reactiveMongoApi, config)
}
```

Inject ReactiveMongoApi on UserDAOImpl.scala and AuthTokenDAOImpl.scala

```scala
class AuthTokenDAOImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends AuthTokenDAO

class UserDAOImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends UserDAO
```

Specify the collection name on both files

```scala
def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("silhouette.user"))

def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("silhouette.token"))
```

Rename password collections if needed on silhouette.conf

```scala
persistence.reactivemongo.collection.OAuth1Info = "silhouette.password"
persistence.reactivemongo.collection.OAuth2Info = "silhouette.password"
persistence.reactivemongo.collection.OpenIDInfo = "silhouette.password"
persistence.reactivemongo.collection.PasswordInfo = "silhouette.password"
```

And finally rewrite functions on the both classes, for example I will show UserDaoImpl functions

```scala
/**
 * Finds a user by its login info.
 *
 * @param loginInfo The login info of the user to find.
 * @return The found user or None if no user for the given login info could be found.
 */
def find(loginInfo: LoginInfo): Future[Option[User]] = {
  val query = Json.obj("loginInfo" -> loginInfo)
  collection.flatMap(_.find(query).one[User])
}

/**
 * Finds a user by its user ID.
 *
 * @param userID The ID of the user to find.
 * @return The found user or None if no user for the given ID could be found.
 */
def find(userID: UUID): Future[Option[User]] = {
  val query = Json.obj("userID" -> userID)
  collection.flatMap(_.find(query).one[User])
}

/**
 * Saves a user.
 *
 * @param user The user to save.
 * @return The saved user.
 */
def save(user: User): Future[User] = {
  collection.flatMap(_.insert(user))
  Future.successful(user)
}
```



