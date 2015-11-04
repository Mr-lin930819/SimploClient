#include "datatrans.h"
#include <QNetworkReply>
#include <QUrlQuery>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>

DataTrans::DataTrans()
{
    manager = new QNetworkAccessManager();
}

void DataTrans::recvLoginPage()
{
    QByteArray content = manager->get(QNetworkRequest(QUrl(LOGIN_URL)))->readAll();
    //获得单次查询会话的Cookie和ViewState,保存。
    QMap<QString,QString> tmpData = convJson2Map(content,"loginInfo");
    loginInfo.viewState = tmpData.value("viewState");
    loginInfo.cookie    = tmpData.value("cookie");
}

void DataTrans::recvCheckCode()
{
    QByteArray imageData = manager->get(QNetworkRequest(QUrl(C_IMG_URL)))->readAll();

    QImage *img = new QImage();
    img->loadFromData(imageData);
    img->save("check.gif","gif");

}

bool DataTrans::tryLogin(QString userName, QString password,QString checkCode)
{
    loginInfo.number = userName;
    loginInfo.password = password;
    loginInfo.checkCode = checkCode;
    QUrlQuery params;
    params.addQueryItem("number",loginInfo.number);
    params.addQueryItem("password",loginInfo.password);
    params.addQueryItem("cookie",loginInfo.cookie);
    params.addQueryItem("checkCode",loginInfo.checkCode);
    params.addQueryItem("viewState",loginInfo.viewState);
    QByteArray canLogin = manager->post(QNetworkRequest(QUrl(TRY_LOGIN_URL)),params.toString().toLatin1())->readAll();
    if(QString(canLogin) == "1")
        return true;
    else
        return false;
}

void DataTrans::setGradeQueryInfo(QString xnStr, QString xqStr)
{
    gradeQueryInfo.number = loginInfo.number;
    gradeQueryInfo.cookie = loginInfo.cookie;
    gradeQueryInfo.xnStr  = xnStr;
    gradeQueryInfo.xqStr  = xqStr;
}


QMap<QString, QString> DataTrans::catchGrade(GradeQueryInfo &info)
{
    QUrlQuery params;
    params.addQueryItem("number",info.number);
    params.addQueryItem("cookie",info.cookie);
    params.addQueryItem("xm",info.xm);
    params.addQueryItem("xnStr",info.xnStr);
    params.addQueryItem("xqStr",info.xqStr);
    QByteArray content = manager->post(QNetworkRequest(QUrl(TRY_LOGIN_URL)),params.toString().toLatin1())->readAll();
    return convJson2Map(content,"GRADE");
}

/**
 * @brief DataTrans::convJson2Map
 * 将json文件中的某个数组转换为Map
 * @param json  json数据
 * @param node  需要转换的节点名
 * @return      map数据
 */
QMap<QString, QString> DataTrans::convJson2Map(QByteArray json,QString node)
{
    QMap<QString,QString> data;

    QJsonObject jsonObj = QJsonDocument::fromJson(json).object();

    QJsonObject jsonArr = jsonObj.value(node).toObject();

    foreach (QString key, jsonArr.keys()) {
        data.insert(key,jsonArr.value(key).toString());
    }

    return data;
}

