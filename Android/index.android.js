'use strict';

import React from 'react';
import {
    AppRegistry,
    StyleSheet,
    Text,
    View,
    Image,
    TouchableHighlight,
    DrawerLayoutAndroid,
    Alert
} from 'react-native';

class HelloWorld extends React.Component {

  static _pressBtn(strg){
    Alert.alert("点击了按钮" + strg)
  };

  constructor() {
    super();
    this.state = {
      sense: 's1'
    }
  }

  _pressTest(se) {
    this.setState({
      sense: se
    });
    this._renderSense();
    this.drawLayout.closeDrawer()
  }

  _renderSense() {
    switch(this.state.sense) {
      case 's1':
        return (
            <View style={{flex: 1, alignItems: 'center'}}>
              <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>Hello</Text>
              <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>World!</Text>
            </View>
        );
      break;
      case 's2':
        return(
            <View style={{flex: 1, alignItems: 'center'}}>
              <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>你好</Text>
              <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>RN世界!</Text>
            </View>
        );
      break;
    }
  }

  sense1 = (
      <View style={{flex: 1, alignItems: 'center'}}>
      <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>Hello</Text>
      <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>World!</Text>
    </View>
  );

  sense2 = (
    <View style={{flex: 1, alignItems: 'center'}}>
      <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>你好</Text>
      <Text style={{margin: 10, fontSize: 15, textAlign: 'right'}}>RN世界!</Text>
    </View>
);

  render() {
    var navigationView = (
        <View style={{flex: 1}}>
          <View style={{flex: 1, backgroundColor: '#fff'}}>
            <Text style={{margin: 10, fontSize: 15, textAlign: 'center'}}>测试的抽屉!</Text>
            <TouchableHighlight onPress={() => {HelloWorld._pressBtn(123)}}
                                style={styles.drawerButton}
                                underlayColor={'darkseagreen'}>
              <Text style={styles.drawerButtonText}>你是谁</Text>
            </TouchableHighlight>
            <TouchableHighlight onPress={this._pressTest.bind(this, 's2')}
                                style={styles.drawerButton}
                                underlayColor={'darkseagreen'}>
              <Text style={styles.drawerButtonText}>你好</Text>
            </TouchableHighlight>
            <TouchableHighlight onPress={this._pressTest.bind(this, 's1')}
                                style={styles.drawerButton}
                                underlayColor={'darkseagreen'}>
              <Text style={styles.drawerButtonText}>我只是个按钮</Text>
            </TouchableHighlight>
            <TouchableHighlight onPress={HelloWorld._pressBtn}
                                style={styles.drawerButton}
                                underlayColor={'darkseagreen'}>
              <Text style={styles.drawerButtonText}>改变按钮</Text>
            </TouchableHighlight><TouchableHighlight onPress={HelloWorld._pressBtn}
                                                     style={styles.drawerButton}
                                                     underlayColor={'darkseagreen'}>
            <Text style={styles.drawerButtonText}>测试按钮</Text>
          </TouchableHighlight>
            <TouchableHighlight onPress={HelloWorld._pressBtn}
                                style={styles.drawerButton}
                                underlayColor={'darkseagreen'}>
              <Text style={styles.drawerButtonText}>结束</Text>
            </TouchableHighlight>

          </View>

          <TouchableHighlight onPress={() => HelloWorld._pressBtn("[设置]")}
                              style={styles.drawerButton}
                              underlayColor={'darkseagreen'}>
            <Text style={styles.drawerButtonText}>设置</Text>
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
            ref = {(drawer)=>{this.drawLayout = drawer}}
            drawerWidth={245}
            drawerPosition={DrawerLayoutAndroid.positions.Left}
            renderNavigationView={() => navigationView}>
          {this._renderSense()}
        </DrawerLayoutAndroid>
    )
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
  drawerButton:{
    height:60,
    justifyContent: 'center',
    backgroundColor:'lightseagreen',
  },
  drawerButtonText: {
    fontSize: 20,
    textAlign: 'center',
    color: 'white',
  },
});

AppRegistry.registerComponent('MainReact', () => HelloWorld);
