dependencies {
    api(project(":api"))

    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    compileOnly("gg.scala.store:velocity:1.0.0")
    compileOnly("gg.scala.commons:velocity:4.3.1")
}
