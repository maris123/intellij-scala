package org.jetbrains.plugins.scala.compiler

import java.io.{File, FileNotFoundException}
import java.nio.charset.StandardCharsets
import java.util.{Base64, Properties}
import javax.crypto.spec.SecretKeySpec

import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.proxy.PropertiesEncryptionSupport

import scala.util.{Failure, Try}

/**
  * @author Maris Alexandru
  */
object HydraCredentialsManager {
  private val LOG = Logger.getInstance(getClass)
  private val HYDRA_CREDENTIALS_FILE = new File(PathManager.getOptionsPath, "hydra.settings.pwd")
  private val HYDRA_LOGIN_KEY = "hydra.login"
  private val HYDRA_PASSWORD_KEY = "hydra.password"
  private val MY_ENCRYPTION_SUPPORT = new PropertiesEncryptionSupport(new SecretKeySpec(Array[Byte](80, 114, 111, 120, 121, 32, 67, 111, 110, 102, 105, 103, 32, 83, 101, 99), "AES"))
  private val MY_HYDRA_CREDENTIALS: Properties = Try({MY_ENCRYPTION_SUPPORT.load(HYDRA_CREDENTIALS_FILE)})
    .recoverWith({
      case fileEx: FileNotFoundException => Failure(fileEx)
      case ex: Throwable => LOG.info(ex)
        Failure(ex)
    }).getOrElse(new Properties())

  implicit def funToRunnable(fun: () => Unit) = new Runnable() {
    def run() = fun()
  }

  def getLogin: String = getSecure(HYDRA_LOGIN_KEY).getOrElse("")

  def setLogin(login: String) = storeSecure(HYDRA_LOGIN_KEY, login)

  def getPlainPassword: String = getSecure(HYDRA_PASSWORD_KEY).getOrElse("")

  def setPlainPassword(password: String) = storeSecure(HYDRA_PASSWORD_KEY, password)

  private def getSecure(key: String): Option[String] = try {
    MY_HYDRA_CREDENTIALS.synchronized {
      Some(MY_HYDRA_CREDENTIALS.getProperty(key, ""))
    }
  } catch {
    case ex: Exception =>
      LOG.info(ex)
      None
  }

  private def storeSecure(key: String, value: String): Unit = {
    if (value == null) removeSecure(key)
    else try {
      MY_HYDRA_CREDENTIALS.synchronized {
        MY_HYDRA_CREDENTIALS.setProperty(key, value)
        MY_ENCRYPTION_SUPPORT.store(MY_HYDRA_CREDENTIALS, "Hydra Credentials", HYDRA_CREDENTIALS_FILE)
      }
    } catch {
      case ex: Exception =>
        LOG.info(ex)
    }
  }

  private def removeSecure(key: String): Unit = {
    try {
      MY_HYDRA_CREDENTIALS.synchronized {
        MY_HYDRA_CREDENTIALS.remove(key)
        MY_ENCRYPTION_SUPPORT.store(MY_HYDRA_CREDENTIALS, "Hydra Credentials", HYDRA_CREDENTIALS_FILE)
      }
    } catch {
      case ex: Exception =>
        LOG.info(ex)
    }
  }
}
