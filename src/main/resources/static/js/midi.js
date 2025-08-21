// ========== 页面加载时获取文件列表 ==========
document.addEventListener("DOMContentLoaded", () => {
    fetch("/midi/list")
        .then(res => res.json())
        .then(data => {
            files = data;
            renderFileList();
        })
        .catch(err => console.error("Failed to load file list:", err));
});

// 渲染文件列表
function renderFileList() {
    const fileList = document.getElementById("fileList");
    fileList.innerHTML = "";
    files.forEach(file => {
        const li = document.createElement("li");
        li.textContent = file.name;
        li.onclick = () => toggleFileDetail(file.id, li);
        fileList.appendChild(li);
    });
}

// ========== 展开 / 收起详情 ==========
function toggleFileDetail(fileId, li) {
    if (currentFileId === fileId) {
        currentFile = null;
        currentFileId = null;
        document.getElementById("fileInfo").innerText = "Select a file to view details";
        document.querySelectorAll("#fileList li").forEach(el => el.classList.remove("active"));
        return;
    }

    // 高亮当前文件
    document.querySelectorAll("#fileList li").forEach(el => el.classList.remove("active"));
    li.classList.add("active");

    // 请求详情
    fetch (`/midi/detail/${fileId}`)
        .then (res => res.json ())
        .then (file => {
            currentFile = file;
            currentFileId = fileId;
            document.getElementById("fileInfo").innerHTML = `
            <div class="info-row"><span class="info-label">Name:</span><span class="info-value">${file.name}</span></div>
            <div class="info-row"><span class="info-label">Description:</span><span class="info-value">${file.description || "None"}</span></div>
            <div class="info-row"><span class="info-label">Instruments:</span><span class="info-value">${file.instrumentCount || 0}</span></div>
            <div class="info-row"><span class="info-label">Created at:</span><span class="info-value">${formatTime(file.createdAt)}</span></div>
            `;
        })
        .catch(err => console.error("Failed to load file detail:", err));
}


// ========== 播放 / 暂停 ==========
function pushMidi() {
    if (!currentFile) {
        alert("Please select a file first!");
        return;
    }
    console.log("Push:", currentFile.name);

    // 这里调用后台的推送接口，比如 /midi/push/{id}
    fetch(`/midi/push/${currentFile.id}`, { method: "POST" })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                alert("✅ Pushed: " + currentFile.name);
            } else {
                alert("❌ Push failed: " + result.message);
            }
        })
        .catch(err => alert("❌ Request error: " + err));
}

function playMidi() {
    if (!currentFile) {
        alert("Please select a file first!");
        return;
    }

    fetch("/midi/play", { method: "POST" })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                console.log("✅ MQTT play command sent");
            } else {
                console.error("❌ Play failed:", result.message);
            }
        });
}

function pauseMidi() {
    if (!currentFile) {
        alert("Please select a file first!");
        return;
    }

    fetch("/midi/pause", { method: "POST" })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                console.log("✅ MQTT pause command sent");
            } else {
                console.error("❌ Pause failed:", result.message);
            }
        });
}

function formatTime(ts) {
    if (!ts) return "Unknown";
    const d = new Date(ts);
    if (isNaN(d.getTime())) {
        // 兜底：如果后端给的是 "2025-08-21T02:17:23" 这种纯字符串
        return String(ts).replace('T', ' ');
    }
    const pad = n => String(n).padStart(2, '0');
    const y = d.getFullYear();
    const m = pad(d.getMonth() + 1);
    const day = pad(d.getDate());
    const hh = pad(d.getHours());
    const mm = pad(d.getMinutes());
    const ss = pad(d.getSeconds());
    return `${y}-${m}-${day} ${hh}:${mm}:${ss}`;
}
