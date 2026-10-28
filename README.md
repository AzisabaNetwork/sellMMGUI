# SellMMGUI

アジ鯖 (Azisaba Network) の LIFE サーバー向けプラグインです。
MythicMobs のアイテム（チケットなど）を GUI に入れて、Vault 経由でお金に換金できます。

## 動作環境

| 項目           | バージョン                                                  |
|----------------|-------------------------------------------------------------|
| サーバー       | Paper 1.21.11                                               |
| Java           | 21 以上                                                     |
| 必須プラグイン | Vault と、Vault に対応した経済プラグイン (EssentialsX など) |
| 推奨プラグイン | MythicMobs 5.x                                              |

アイテムの判定には、MythicMobs 5.x がアイテムに書き込む PersistentDataContainer (`mythicmobs:type`) を使います。
そのため LifeCore への依存はなくなりました。

## 使い方

1. `/smg` を実行すると売却 GUI が開きます。
2. 上の 5 段に売りたいアイテムを入れます（シフトクリックや「一括投入」ボタンでも入れられます）。
3. **売却する** を押すと売却されます。ボタンの説明欄で、売却予定額をリアルタイムに確認できます。

### GUI のボタン（最下段）

| ボタン                    | 内容                                                     |
|---------------------------|----------------------------------------------------------|
| 本 売却可能アイテム一覧   | 売れるアイテムと単価を表示（クリックでチャットにも表示） |
| ホッパー 一括投入         | 手持ちの売却可能アイテムをまとめて GUI に移す            |
| 黄色ガラス 使い方         | 操作方法の説明                                           |
| エメラルド 売却する       | 売却予定額・個数を表示し、クリックで売却                 |
| チェスト 中身をすべて返却 | 売却せずに中身をインベントリへ戻す                       |
| バリア 閉じる             | GUI を閉じる                                             |

- **売却できないアイテムは消えずに返却されます。**（旧バージョンでは消えていました）
- インベントリに入りきらないアイテムは足元にドロップします。
- `sell-on-close: true`（初期値）の場合は、GUI を閉じたときにも売却されます。

## コマンド・権限

| コマンド            | 説明                                              | 権限                                 | 初期値 |
|---------------------|---------------------------------------------------|--------------------------------------|--------|
| `/smg`              | 売却 GUI を開く                                   | `SellMMGUICommand.permission.Member` | 全員   |
| `/smg list`         | 売却可能アイテム一覧をチャットに表示              | `SellMMGUICommand.permission.Member` | 全員   |
| `/sellmmgui reload` | `config.yml` を再読み込み                         | `SellMMGUICommand.permission.Admin`  | OP     |
| `/sellmmgui check`  | 手に持ったアイテムの MythicMobs ID と売却額を表示 | `SellMMGUICommand.permission.Admin`  | OP     |

## 設定 (`config.yml`)

```yaml
mmitem:
  yukiti_ticket:        # MythicMobs のアイテム ID (大文字・小文字は区別しない)
    sellprice: 10000    # 1 個あたりの売却額
    display: "&3一万円"  # 一覧に表示する名前 (省略時は ID)

gui-title: "&cSELLMMITEM SHOP"
sell-on-close: true     # GUI を閉じたときにも売却するか

tomato-drop:            # 葉ブロックからトマトを入手する機能 (初期値は無効)
  enabled: false
  world: resource
  block: OAK_LEAVES
  chance: 0.1
  tool-mythic-type: ""  # 設定すると道具を MythicMobs ID で判定する
  tool-name: "&cトマト採取剣"
  command: "mm i give {player} tomato 2"

messages:               # 各種メッセージ (& カラーコード可)
  ...
```

旧バージョンの `mmitem.<ID>.sellprice` の形式はそのまま使えます。
`SellItemDisplay` は廃止しました。一覧は `mmitem` の内容から自動で作られます。

## ビルド

```bash
./gradlew build
```

`build/libs/SellMMGUI-<version>.jar` ができます（Kotlin 標準ライブラリはリロケートして同梱しています）。

テストサーバーを起動するときは次のコマンドを使います（Vault と経済プラグインは `run/plugins` に別途入れてください）。

```bash
./gradlew runServer
```

## 2.0.0 での主な変更

- Minecraft 1.15.2 (Spigot) から 1.21.11 (Paper) に対応
- Java から Kotlin に移行し、Maven から Gradle Kotlin DSL に移行
- `/smg` で売却 GUI を直接開くようにした（メニュー画面を廃止）
- 売却予定額のリアルタイム表示、一括投入、全返却のボタンを追加
- 売却できないアイテムを消さずに返却するようにした
- LifeCore への依存をなくし、MythicMobs の PDC から ID を読むようにした
- メッセージ・GUI タイトルを設定できるようにした。タブ補完に対応
- コンソールからコマンドを実行してもエラーにならないようにした
- トマト入手機能は、これまでリスナーが登録されておらず動いていなかったため、設定で有効化できる形にした（初期値は無効）

## ライセンス

[GPL-3.0](LICENSE)
