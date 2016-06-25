package com.github.alechenninger.chronicler.toggl;

class UserAndPassword {
  final String user;
  final String password;

  UserAndPassword(String user, String password) {
    this.user = user;
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserAndPassword that = (UserAndPassword) o;

    if (user != null ? !user.equals(that.user) : that.user != null) return false;
    return password != null ? password.equals(that.password) : that.password == null;

  }

  @Override
  public int hashCode() {
    int result = user != null ? user.hashCode() : 0;
    result = 31 * result + (password != null ? password.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "UserAndPassword{" +
        "user='" + user + '\'' +
        ", password='" + password + '\'' +
        '}';
  }
}
