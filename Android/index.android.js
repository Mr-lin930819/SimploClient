'use strict';

import React from 'react';
import {
    AppRegistry,
    StyleSheet,
    Text,
    View,
    Image,
    TouchableHighlight,
    TouchableWithoutFeedback,
    DrawerLayoutAndroid,
    Alert,
    Modal,
    Picker
} from 'react-native';

class HelloWorld extends React.Component {

    static _pressBtn(strg) {
        Alert.alert("点击了按钮" + strg)
    };

    constructor() {
        super();
        this.state = {
            sense: 's1',
            xnSelectDialogShow: false,
            selectedXn: ''
        }
    }

    _pressTest(se) {
        this.setState({
            sense: se,
            xnSelectDialogShow: true
        });
        // this._renderSense();
        this.drawLayout.closeDrawer()
    }

    _renderSense() {
        switch (this.state.sense) {
            case 's1':
                return (
                    <View style={{flex: 1, alignItems: 'center'}}>
                        <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>Hello</Text>
                        <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>World!</Text>
                    </View>
                );
                break;
            case 's2':
                return (
                    <View style={{flex: 1, alignItems: 'center'}}>
                        <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>你好</Text>
                        <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>RN世界!</Text>
                    </View>
                );
                break;
            case 'exam':
                return this._renderExamPage();
                break;
        }
    }

    render() {
        var navigationView = (
            <View style={{flex: 1}}>
                <View style={{flex: 1, backgroundColor: '#fff'}}>
                    <Text style={{margin: 10, fontSize: 15, textAlign: 'center'}}>测试的抽屉!</Text>
                    <TouchableHighlight onPress={this._pressTest.bind(this, 'exam')}
                                        style={styles.drawerButton}
                                        underlayColor={'darkseagreen'}>
                        <Text style={styles.drawerButtonText}>查询考试信息</Text>
                    </TouchableHighlight>
                    <TouchableHighlight onPress={this._pressTest.bind(this, 's2')}
                                        style={styles.drawerButton}
                                        underlayColor={'darkseagreen'}>
                        <Text style={styles.drawerButtonText}>查询等级考试信息</Text>
                    </TouchableHighlight>
                    <TouchableHighlight onPress={this._pressTest.bind(this, 's1')}
                                        style={styles.drawerButton}
                                        underlayColor={'darkseagreen'}>
                        <Text style={styles.drawerButtonText}>查询成绩</Text>
                    </TouchableHighlight>
                    <TouchableHighlight onPress={HelloWorld._pressBtn}
                                        style={styles.drawerButton}
                                        underlayColor={'darkseagreen'}>
                        <Text style={styles.drawerButtonText}>查询课表</Text>
                    </TouchableHighlight><TouchableHighlight onPress={HelloWorld._pressBtn}
                                                             style={styles.drawerButton}
                                                             underlayColor={'darkseagreen'}>
                    <Text style={styles.drawerButtonText}>查询学分/绩点</Text>
                </TouchableHighlight>

                </View>

                <TouchableHighlight onPress={() => HelloWorld._pressBtn("[设置]")}
                                    style={styles.drawerButton}
                                    underlayColor={'darkseagreen'}>
                    <Text style={styles.drawerButtonText}>设置</Text>
                </TouchableHighlight>
                <TouchableHighlight onPress={() => HelloWorld._pressBtn("[设置]")}
                                    style={styles.drawerButton}
                                    underlayColor={'darkseagreen'}>
                    <Text style={styles.drawerButtonText}>登出</Text>
                </TouchableHighlight>
            </View>
        );

        return (
            // <View style={styles.container}>
            //
            //   <Text style={styles.hello}>Hello, World</Text>
            //
            //   <Text style={styles.hello}>这是一个React Native程序！</Text>
            //
            //   <Text style={styles.hello}>还真挺不错的呀</Text>
            //   <Image source={{uri: 'https://facebook.github.io/react/img/logo_og.png'}}
            //  style={{width: 100, height: 100}} />
            // </View>

            <DrawerLayoutAndroid
                ref={(drawer)=> {
                    this.drawLayout = drawer
                }}
                drawerWidth={245}
                drawerPosition={DrawerLayoutAndroid.positions.Left}
                renderNavigationView={() => navigationView}>
                {this._renderSense()}
            </DrawerLayoutAndroid>
        )
    }

    pickData = ['', '2012-2013', '2013-2014', '2014-2015', '2015-2016'];


    _renderExamPage() {
        return (
            <View>
                <Modal visible={this.state.xnSelectDialogShow}
                       onRequestClose={()=> {
                       }}>
                    <View
                        style={{flex: 1, justifyContent: 'center', padding: 20, backgroundColor: 'rgba(0, 0, 0, 0.5)'}}>
                        <View style={{backgroundColor: 'white'}}>
                            <Text style={styles.hello}>选择学年学期</Text>
                            <Picker selectedValue={this.state.selectedXn}
                                    onValueChange={(va) => {
                                        this.setState({selectedXn: va})
                                    }}>
                                {/*{this.pickData.map((item) =>*/}
                                    {/*<Picker.Item label={item.text} key={item.id}/>)}*/}
                                {this._renderItem(this.pickData)}
                            </Picker>
                            <TouchableHighlight onPress={()=> {
                                this.setState({xnSelectDialogShow: false})
                            }}
                                                style={styles.drawerButton}
                                                underlayColor={'blue'}>
                                <Text style={styles.drawerButtonText}>关闭</Text>
                            </TouchableHighlight>
                        </View>
                    </View>
                </Modal>
            </View>);
        //     <View style={{flex: 1, alignItems: 'center'}}>
        //         <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>你好</Text>
        //         <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>RN世界!</Text>
        //     </View>
        // )
    }

    _renderItem(xnArray) {
        var arr = [];
        for(let i=0;i<xnArray.length;i++) {
            arr.push(<Picker.Item label={xnArray[i]} id={i+''}/>)
        }
        return arr
    }
}
var styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
    },
    hello: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    drawerButton: {
        height: 60,
        justifyContent: 'center',
        backgroundColor: 'lightseagreen',
    },
    drawerButtonText: {
        fontSize: 20,
        textAlign: 'center',
        color: 'white',
    },
});

AppRegistry.registerComponent('MainReact', () => HelloWorld);
