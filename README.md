# DiscordWhitelist

將 Discord Slash Command 與 Minecraft 白名單整合的 Paper/Spigot 插件。

玩家可以在 Discord 執行 `/whitelist`，插件會開啟 Modal，讓使用者填寫 Java 版與基岩版玩家名稱，送出後自動更新 Minecraft 伺服器白名單。

## 功能

- Discord Slash Command：`/whitelist`
- Discord Modal 輸入 Java 版與基岩版玩家名稱
- 兩個欄位皆可留白，但至少需要填寫一個
- 基岩版玩家名稱會自動補上前綴 `.`
- 自動檢查玩家是否已在白名單
- 新增後立即生效
- 所有訊息與 Discord 設定皆可透過 `config.yml` 自訂
- 同時支援 Minecraft 內的 `/白名單` 與 `/whitelist` 指令

## 系統需求

- Paper 或 Spigot 1.20+
- Java 17 或更新版本
- Maven 3.9+
- Discord Bot Token

## 安裝

1. 從 GitHub Releases 下載 `DiscordWhitelist-1.0.0.jar`。
2. 將 JAR 放入伺服器的 `plugins` 資料夾。
3. 啟動一次伺服器，讓插件產生設定檔。
4. 編輯 `plugins/DiscordWhitelist/config.yml`。
5. 填入 Discord Bot Token 與其他設定。
6. 重新啟動伺服器。

## Discord Bot 設定

前往 [Discord Developer Portal](https://discord.com/developers/applications)：

1. 建立一個 Application。
2. 進入 **Bot** 頁面，建立 Bot 並複製 Token。
3. 進入 **OAuth2 > URL Generator**。
4. 勾選以下 Scopes：
   - `bot`
   - `applications.commands`
5. 將產生的邀請網址開啟，邀請 Bot 加入伺服器。
6. 將 Token 填入 `config.yml`。

請勿將 Token 提交到公開 GitHub Repository。若 Token 外洩，請立刻在 Discord Developer Portal 重新產生 Token。

## 設定檔

```yml
discord:
  enabled: true
  token: "PUT_YOUR_BOT_TOKEN_HERE"
  command-name: "whitelist"
  guild-id: ""
  command-permission: ""
```

### 設定說明

| 設定 | 說明 |
| --- | --- |
| `discord.enabled` | 是否啟用 Discord 整合。 |
| `discord.token` | Discord Bot Token。請勿公開。 |
| `discord.command-name` | Discord Slash Command 名稱，預設為 `whitelist`。只能使用 Discord 支援的指令名稱格式。 |
| `discord.guild-id` | Discord 伺服器 ID。填寫後會註冊為伺服器指令，通常會立即出現；留空則註冊為全域指令，可能需要等待最多一小時。 |
| `discord.command-permission` | 可選的 Discord 權限名稱，例如 `MANAGE_SERVER`；留空代表不額外限制。 |

### Discord

在已加入 Bot 的 Discord 伺服器執行：

```text
/whitelist
```

接著在 Modal 填入：

- **Java 版**：例如 `Steve`
- **基岩版**：例如 `Alex`，不需要輸入 `.`

如果輸入基岩版名稱 `Alex`，插件會自動以 `.Alex` 加入 Minecraft 白名單。

### Minecraft

不使用 Discord 時，也可以在遊戲內執行：

```text
/白名單
```

或使用參數直接新增：

```text
/白名單 <Java版名稱> [基岩版名稱]
```

使用 Minecraft 指令需要 `discordwhitelist.use` 權限。此權限預設開放給所有玩家，可使用權限插件進行限制。

此 JAR 已包含 JDA 依賴，可以直接放入伺服器的 `plugins` 資料夾。

## 常見問題

### Discord 指令沒有出現

- 確認 Bot 已使用 `applications.commands` Scope 邀請到伺服器。
- 確認 `discord.enabled` 是 `true`。
- 確認 Token 正確且沒有多餘空白。
- 若 `guild-id` 留空，全域指令可能需要等待最多一小時。
- 查看伺服器啟動日誌是否出現 Discord 連線錯誤。

### 基岩版玩家名稱為什麼有 `.`？

這是伺服器辨識基岩版玩家名稱的格式。使用者在 Discord 或 Minecraft 指令中不需要自行輸入，插件會自動補上。

## 授權

目前尚未指定開源授權。