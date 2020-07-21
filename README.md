싫어하면 울리는
=============
# 개요

몰입캠프 2주차 과제: 서버를 활용한 앱 제작

# 조원

[조민규](https://github.com/Mingyu-Lucif)

[홍은지](https://github.com/eunzihong)

[임성재](https://github.com/imsj114)

# 설명

## 전체 구조

처음 접속할 시 Splash Activity -> Login Activity -> Main Activity 순으로 연결되며,
Facebook의 User Profil API를 사용해 로그인할 수 있도록 하였다.
이미 로그인이 된 경우에는 Login Activity가 스킵된다.

Main Activity에서는 Bottom Navigation을 통해 3개의 탭을 이동할 수 있다.
또한 Main Activity에서 Drawer Layout을 통해 현재 로그인 상태를 확인할 수 있다.

주요 기능은 총 3가지로, 서버에 연락처를 저장하고 관리할 수 있는 기능, 서버에 이미지를 저장하고 추가/삭제할 수 있는 기능, 그리고 "싫어하는 사람"의 실시간 위치를 확인해 알림을 받을 수 있는 기능으로 구성되어 있다.

MVVM Model을 사용하여 View, Viewmodel, 그리고 Model로 데이터 Hierarchy를 구성하여 Scalable한 데이터 관리를 할 수 있도록 앱을 제작했다.

## 1. 연락처 TAB

- Recyclerview를 최상위 Layout으로 사용하여 서버에 저장된 연락처를 표시하도록 했다.
- 우측 하단에 위치한 Floating Action Button은 두 개로 구성되어 있는데, 동기화 버튼과 연락처 추가 버튼이다.
  - 연락처 동기화는 접속 또는 추가시 자동으로 되기 때문에, Faulty Connection 문제로 Manual Override가 필요한 상황을 대비하여 만들어졌다.
  - 연락처 추가 버튼을 누를 시 Alert Dialog를 표시하여 사용자가 연락처 정보를 입력할 수 있도록 하였다. 추가된 연락처는 자동으로 서버에 등록되어 로컬 연락처 리스트와 동기화된다.

## 2. 갤러리 TAB

- 갤러리 또한 연락처 TAB과 비슷하게 Recycler View를 사용하여 서버에 저장된 사용자의 이미지를 나타낸다.
- 마찬가지로 동기화 버튼과 이미지 추가 버튼이 

## 3.  싫어하면 울리는 TAB

- 