// ========== 页面加载时获取文件列表 ==========
document.addEventListener("DOMContentLoaded", () => {
    fetch("/midi/list")
        .then(res => res.json())
        .then(data => {
            files = data;
            renderFileList();
        })
        .catch(err => console.error("加载文件列表失败:", err));
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
        document.getElementById("fileInfo").innerText = "请选择一个文件查看详情";
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
            document.getElementById ("fileInfo").innerHTML = `
            <div class="info-row"><span class="info-label">文件名:</span><span class="info-value">${file.name}</span></div>
            <div class="info-row"><span class="info-label">描述:</span><span class="info-value">${file.description || "无"}</span></div>
            <div class="info-row"><span class="info-label">乐器数量:</span><span class="info-value">${file.instrumentCount || 0}</span></div>
            <div class="info-row"><span class="info-label">创建时间:</span><span class="info-value">${file.createdAt || "未知"}</span></div>
            `;
        })
        .catch (err => console.error ("加载文件详情失败:", err));
}


// ========== 播放 / 暂停 ==========
function pushMidi() {
    if (!currentFile) {
        alert("请先选择一个文件！");
        return;
    }
    console.log("推送:", currentFile.name);

    // 这里调用后台的推送接口，比如 /midi/push/{id}
    fetch(`/midi/push/${currentFile.id}`, { method: "POST" })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                alert("✅ 已推送: " + currentFile.name);
            } else {
                alert("❌ 推送失败: " + result.message);
            }
        })
        .catch(err => alert("❌ 请求错误: " + err));
}

function playMidi() {
    if (!currentFile) {
        alert("请先选择一个文件！");
        return;
    }

    fetch("/midi/play", { method: "POST" })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                console.log("✅ MQTT 播放指令已发送");
            } else {
                console.error("❌ 播放失败:", result.message);
            }
        });
}

function pauseMidi() {
    if (!currentFile) {
        alert("请先选择一个文件！");
        return;
    }

    fetch("/midi/pause", { method: "POST" })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                console.log("✅ MQTT 暂停指令已发送");
            } else {
                console.error("❌ 暂停失败:", result.message);
            }
        });
}
