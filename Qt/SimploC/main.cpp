#include <QApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QtQml>
#include "datatrans.h"

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);
    qmlRegisterType<DataTrans>("DataTrans",1,0,"DataTransType");
    QQmlApplicationEngine engine;

    engine.load(QUrl(QStringLiteral("qrc:/main.qml")));

    return app.exec();
}
