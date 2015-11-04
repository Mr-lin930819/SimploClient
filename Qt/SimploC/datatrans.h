#ifndef DATATRANS_H
#define DATATRANS_H

#include <QBitmap>
#include <QMap>
#include <QNetworkAccessManager>
#include <QObject>

#define HOST_URL        "http://172.20.27.41:8080/SimploServer"
#define LOGIN_URL       HOST_URL"/OneServlet"
#define TRY_LOGIN_URL   HOST_URL"/TwoServlet"
#define CATCH_GRADE_URL HOST_URL"/ThreeServlet"
#define C_IMG_URL       HOST_URL"/FourServlet"

struct LoginInfo{
    QString number;
    QString password;
    QString checkCode;
    QString cookie;
    QString viewState;
};
struct GradeQueryInfo{
    QString cookie;
    QString number;
    QString xm;
    QString xnStr;
    QString xqStr;
};

class DataTrans:public QObject
{
    Q_OBJECT
public:
    DataTrans();
    Q_INVOKABLE void recvLoginPage();
    Q_INVOKABLE void recvCheckCode();
    Q_INVOKABLE bool tryLogin(QString userName, QString password, QString checkCode);
    Q_INVOKABLE void setGradeQueryInfo(QString xnStr,QString xqStr);
    Q_INVOKABLE QMap<QString,QString> catchGrade(GradeQueryInfo &info);

private:
    QMap<QString,QString> convJson2Map(QByteArray json, QString node);
private:
//    QString stuNumber;
//    QString password;
//    QString viewState;
//    QString cookie;
    QNetworkAccessManager *manager;
    LoginInfo loginInfo;
    GradeQueryInfo gradeQueryInfo;
};

#endif // DATATRANS_H
