package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import play.api.db._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def usersettings = Action {
    Ok(views.html.usersettings())
  }

  def algorithmsettings = Action {
    Ok(views.html.algorithmsettings())
  }
}
