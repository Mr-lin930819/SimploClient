/****************************************************************************
** Meta object code from reading C++ file 'datatrans.h'
**
** Created by: The Qt Meta Object Compiler version 67 (Qt 5.5.0)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "../datatrans.h"
#include <QtCore/qbytearray.h>
#include <QtCore/qmetatype.h>
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'datatrans.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 67
#error "This file was generated using the moc from 5.5.0. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

QT_BEGIN_MOC_NAMESPACE
struct qt_meta_stringdata_DataTrans_t {
    QByteArrayData data[15];
    char stringdata0[160];
};
#define QT_MOC_LITERAL(idx, ofs, len) \
    Q_STATIC_BYTE_ARRAY_DATA_HEADER_INITIALIZER_WITH_OFFSET(len, \
    qptrdiff(offsetof(qt_meta_stringdata_DataTrans_t, stringdata0) + ofs \
        - idx * sizeof(QByteArrayData)) \
    )
static const qt_meta_stringdata_DataTrans_t qt_meta_stringdata_DataTrans = {
    {
QT_MOC_LITERAL(0, 0, 9), // "DataTrans"
QT_MOC_LITERAL(1, 10, 13), // "recvLoginPage"
QT_MOC_LITERAL(2, 24, 0), // ""
QT_MOC_LITERAL(3, 25, 13), // "recvCheckCode"
QT_MOC_LITERAL(4, 39, 8), // "tryLogin"
QT_MOC_LITERAL(5, 48, 8), // "userName"
QT_MOC_LITERAL(6, 57, 8), // "password"
QT_MOC_LITERAL(7, 66, 9), // "checkCode"
QT_MOC_LITERAL(8, 76, 17), // "setGradeQueryInfo"
QT_MOC_LITERAL(9, 94, 5), // "xnStr"
QT_MOC_LITERAL(10, 100, 5), // "xqStr"
QT_MOC_LITERAL(11, 106, 10), // "catchGrade"
QT_MOC_LITERAL(12, 117, 21), // "QMap<QString,QString>"
QT_MOC_LITERAL(13, 139, 15), // "GradeQueryInfo&"
QT_MOC_LITERAL(14, 155, 4) // "info"

    },
    "DataTrans\0recvLoginPage\0\0recvCheckCode\0"
    "tryLogin\0userName\0password\0checkCode\0"
    "setGradeQueryInfo\0xnStr\0xqStr\0catchGrade\0"
    "QMap<QString,QString>\0GradeQueryInfo&\0"
    "info"
};
#undef QT_MOC_LITERAL

static const uint qt_meta_data_DataTrans[] = {

 // content:
       7,       // revision
       0,       // classname
       0,    0, // classinfo
       5,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // methods: name, argc, parameters, tag, flags
       1,    0,   39,    2, 0x02 /* Public */,
       3,    0,   40,    2, 0x02 /* Public */,
       4,    3,   41,    2, 0x02 /* Public */,
       8,    2,   48,    2, 0x02 /* Public */,
      11,    1,   53,    2, 0x02 /* Public */,

 // methods: parameters
    QMetaType::Void,
    QMetaType::Void,
    QMetaType::Bool, QMetaType::QString, QMetaType::QString, QMetaType::QString,    5,    6,    7,
    QMetaType::Void, QMetaType::QString, QMetaType::QString,    9,   10,
    0x80000000 | 12, 0x80000000 | 13,   14,

       0        // eod
};

void DataTrans::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        DataTrans *_t = static_cast<DataTrans *>(_o);
        Q_UNUSED(_t)
        switch (_id) {
        case 0: _t->recvLoginPage(); break;
        case 1: _t->recvCheckCode(); break;
        case 2: { bool _r = _t->tryLogin((*reinterpret_cast< QString(*)>(_a[1])),(*reinterpret_cast< QString(*)>(_a[2])),(*reinterpret_cast< QString(*)>(_a[3])));
            if (_a[0]) *reinterpret_cast< bool*>(_a[0]) = _r; }  break;
        case 3: _t->setGradeQueryInfo((*reinterpret_cast< QString(*)>(_a[1])),(*reinterpret_cast< QString(*)>(_a[2]))); break;
        case 4: { QMap<QString,QString> _r = _t->catchGrade((*reinterpret_cast< GradeQueryInfo(*)>(_a[1])));
            if (_a[0]) *reinterpret_cast< QMap<QString,QString>*>(_a[0]) = _r; }  break;
        default: ;
        }
    }
}

const QMetaObject DataTrans::staticMetaObject = {
    { &QObject::staticMetaObject, qt_meta_stringdata_DataTrans.data,
      qt_meta_data_DataTrans,  qt_static_metacall, Q_NULLPTR, Q_NULLPTR}
};


const QMetaObject *DataTrans::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->dynamicMetaObject() : &staticMetaObject;
}

void *DataTrans::qt_metacast(const char *_clname)
{
    if (!_clname) return Q_NULLPTR;
    if (!strcmp(_clname, qt_meta_stringdata_DataTrans.stringdata0))
        return static_cast<void*>(const_cast< DataTrans*>(this));
    return QObject::qt_metacast(_clname);
}

int DataTrans::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QObject::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 5)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 5;
    } else if (_c == QMetaObject::RegisterMethodArgumentMetaType) {
        if (_id < 5)
            *reinterpret_cast<int*>(_a[0]) = -1;
        _id -= 5;
    }
    return _id;
}
QT_END_MOC_NAMESPACE
