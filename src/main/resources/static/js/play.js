// 实时监测状态
// WebSocket 连接
// band-status.js — minimal version for "ready" test
// 建立 WebSocket 连接
const ws = new WebSocket("ws://localhost:8080/ws/status");

ws.onopen = () => {
    console.log("✅ WebSocket 已连接");
};

ws.onmessage = (event) => {
    console.log("📩 收到消息:", event.data);

    try {
        const msg = JSON.parse(event.data);
        const instrument = msg.instrument;
        const status = msg.status;

        // 找到表格里对应的单元格，例如 <td id="Piano">
        const cell = document.getElementById(instrument);
        if (cell) {
            cell.textContent = status;

            // 根据状态设置样式
            switch (status) {
                case "ready":
                    cell.style.color = "green";
                    break;
                case "playing":
                    cell.style.color = "blue";
                    break;
                case "online":
                    cell.style.color = "red";
                    break;
                default:
                    cell.style.color = "black";
            }
        }
    } catch (e) {
        console.error("❌ JSON 解析失败:", e);
    }
};

ws.onclose = () => {
    console.log("❌ WebSocket 已关闭");
};

ws.onerror = (err) => {
    console.error("⚠️ WebSocket 出错:", err);
};
