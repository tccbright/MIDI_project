// 模拟从数据库获取文件
let files = [
    { id: 1, name: "demo1.mid", desc: "测试歌曲1" },
    { id: 2, name: "demo2.mid", desc: "测试歌曲2" }
];

function renderFileList() {
    const ul = document.getElementById("fileList");
    ul.innerHTML = "";
    files.forEach(f => {
        const li = document.createElement("li");
        li.textContent = f.name;
        li.ondblclick = () => showFileInfo(f);
        ul.appendChild(li);
    });
}

function showFileInfo(file) {
    const infoBox = document.getElementById("fileInfo");
    infoBox.innerHTML = `
    <strong>歌曲名:</strong> ${file.name}<br>
    <strong>简介:</strong> ${file.desc || "无"}
  `;
}

// 上传 MIDI 弹窗控制
function openUploadModal() {
    document.getElementById("uploadModal").style.display = "block";
}

function closeUploadModal() {
    document.getElementById("uploadModal").style.display = "none";
}




// 播放 & 暂停模拟
function playMidi() {
    document.getElementById("bandStatus").textContent = "▶ 播放中...";
}

function pauseMidi() {
    document.getElementById("bandStatus").textContent = "⏸ 已暂停";
}

// 初始化文件列表
renderFileList();
