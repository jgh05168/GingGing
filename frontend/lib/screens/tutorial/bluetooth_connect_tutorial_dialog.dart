import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:frontend/core/theme/constant/app_colors.dart';
import 'package:frontend/core/theme/constant/app_icons.dart';
import 'package:frontend/core/theme/custom/custom_font_style.dart';
import 'package:go_router/go_router.dart';

class BluetoothConnectTutorialDialog extends StatefulWidget {
  const BluetoothConnectTutorialDialog({super.key});

  @override
  State<BluetoothConnectTutorialDialog> createState() =>
      _BluetoothConnectTutorialDialogState();
}

class _BluetoothConnectTutorialDialogState extends State<BluetoothConnectTutorialDialog>
    with TickerProviderStateMixin {
  AnimationController? _animationController_ready_button;
  Animation<double>? _scaleAnimation_ready_button;
  AnimationController? _animationController_earth;
  Animation<double>? _scaleAnimation_earth;
  AnimationController? _animationController_speak_bubble;
  Animation<double>? _scaleAnimation_speak_bubble;
  bool buttonCheck = false;

  @override
  void initState() {
    super.initState();
    _animationController_ready_button = AnimationController(
        duration: const Duration(milliseconds: 500), vsync: this);
    _scaleAnimation_ready_button = Tween<double>(begin: 1, end: 1.1)
        .animate(_animationController_ready_button!);

    _animationController_earth = AnimationController(
        duration: const Duration(milliseconds: 1000), vsync: this);
    _scaleAnimation_earth =
        Tween<double>(begin: 0, end: 1).animate(_animationController_earth!);

    _animationController_speak_bubble = AnimationController(
        duration: const Duration(milliseconds: 1000), vsync: this);
    _scaleAnimation_speak_bubble = Tween<double>(begin: 0, end: 1)
        .animate(_animationController_speak_bubble!);

      _animationController_earth!.forward();
      _animationController_speak_bubble!.forward();
    Future.delayed(const Duration(milliseconds: 1200), () {
      setState(() {
        buttonCheck = true;
      });
      _animationController_ready_button!.repeat(reverse: true);
    });
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () {
        Fluttertoast.showToast(msg: '시작하기 버튼을 눌러주세요!');
      },
      child: Scaffold(
        backgroundColor: Colors.transparent,
        body: Stack(
          children: [
            Positioned(
              bottom: MediaQuery.of(context).size.height * 0,
              right: MediaQuery.of(context).size.width * 0.24,
              child: AnimatedBuilder(
                animation: _scaleAnimation_earth!,
                builder: (context, widget) {
                  if (_scaleAnimation_earth != null) {
                    return Transform.scale(
                      scale: _scaleAnimation_earth!.value,
                      child: widget,
                    );
                  } else {
                    return Container();
                  }
                },
                child: Container(
                  child: Image.asset(AppIcons.earth_2),
                ),
              ),
            ),
            Positioned(
              bottom: MediaQuery.of(context).size.height * 0.3,
              right: MediaQuery.of(context).size.width * 0.01,
              child: AnimatedBuilder(
                animation: _scaleAnimation_speak_bubble!,
                builder: (context, widget) {
                  if (_scaleAnimation_speak_bubble != null) {
                    return Transform.scale(
                      scale: _scaleAnimation_speak_bubble!.value,
                      child: widget,
                    );
                  } else {
                    return Container();
                  }
                },
                child: Container(
                    width: MediaQuery.of(context).size.width * 0.7,
                    child: Stack(children: [
                      Image.asset(AppIcons.intro_speak_bubble),
                      Positioned(
                        left: MediaQuery.of(context).size.width * 0.14,
                        top: MediaQuery.of(context).size.height * 0.03,
                        child: Container(
                          child: Column(
                            children: [
                              Text(
                                '버튼을 눌러서',
                                style: CustomFontStyle.getTextStyle(
                                    context, CustomFontStyle.yeonSung100),
                              ),
                              Text(
                                '깅 스틱과 블루투스를',
                                style: CustomFontStyle.getTextStyle(
                                    context, CustomFontStyle.yeonSung100),
                              ),
                              Text(
                                '연결해봐',
                                style: CustomFontStyle.getTextStyle(
                                    context, CustomFontStyle.yeonSung100),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],)
                ),
              ),
            ),
            buttonCheck ?
            Positioned(
              right: MediaQuery.of(context).size.width * 0.026,
              bottom: MediaQuery.of(context).size.width * 0.06,
              child: AnimatedBuilder(
                animation: _scaleAnimation_ready_button!,
                builder: (context, widget) {
                  if (_scaleAnimation_ready_button != null) {
                    return Transform.scale(
                      scale: _scaleAnimation_ready_button!.value,
                      child: widget,
                    );
                  } else {
                    return Container();
                  }
                },
                child: GestureDetector(
                  onTap: () {
                    Navigator.of(context).pop();
                  },
                  child: Container(
                    width: MediaQuery.of(context).size.width * 0.3,
                    height: MediaQuery.of(context).size.height * 0.05,
                    decoration: BoxDecoration(
                      color: AppColors.readyButton,
                      borderRadius: BorderRadius.circular(30),
                      border: Border.all(width: 3, color: Colors.white),
                      boxShadow: [
                        BoxShadow(
                          color: AppColors.basicgray.withOpacity(0.5),
                          offset: const Offset(0, 4),
                          blurRadius: 1,
                          spreadRadius: 1,
                        )
                      ],
                    ),
                    child: Stack(
                      children: [
                        Positioned(
                          top: MediaQuery.of(context).size.height * 0.003,
                          left: MediaQuery.of(context).size.width * 0.015,
                          child: Container(
                            child: const Icon(
                              Icons.directions_run_sharp,
                              size: 30,
                              color: Colors.white,
                            ),
                          ),
                        ),
                        Center(
                          child: Text(
                            '   시작하기',
                            style: CustomFontStyle.getTextStyle(
                                context, CustomFontStyle.yeonSung80_white),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ) : Container(),
          ],
        ),
      ),
    );
  }
}
