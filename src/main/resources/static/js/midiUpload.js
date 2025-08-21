let files = [];              // 全部文件列表
let currentFile = null;      // 当前选中的文件
let currentFileId = null;    // 当前选中文件 id

// ========== 上传 MIDI ==========
async function uploadMidi(action) {
    const fileInput = document.getElementById("file");
    const nameInput = document.getElementById("name");
    const descInput = document.getElementById("description");

    if (!fileInput.files.length) {
        alert("Please select a MIDI file!");
        return;
    }

    if (!nameInput.value.trim()) {
        alert("Please enter the song name!");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("name", nameInput.value.trim());
    formData.append("description", descInput.value.trim());

    const url = "/midi/" + action; // /midi/save or /midi/savePublish

    try {
        const res = await fetch(url, {
            method: "POST",
            body: formData
        });

        if (!res.ok) throw new Error("HTTP " + res.status);

        const result = await res.json();   // {success,message,data}

        if (result.success) {
            const song = result.data;

            // 更新上传结果提示（弹窗内）
            document.getElementById("result").innerText = "✅ Upload success: " + song.name;

            // 在同一个上传卡片里显示最近上传信息
            const html = `
                <h3 style="margin-top:12px;">📑 Recent Upload</h3>
                <div class="last-upload-box">
                    <p>🎶 <b>Song:</b> ${song.name}</p>
                    <p><b>Description:</b> ${song.description || "N/A"}</p>
                    <p><b>Instrument count:</b> ${song.instrumentCount}</p>
                </div>
            `;
            document.getElementById("lastUpload").innerHTML = html;

            // 关闭模态框
            closeUploadModal();

            // 同时把新文件加到右侧列表
            files.push({
                id: song.id || Date.now(),
                name: song.name
            });
            renderFileList();
        } else {
            document.getElementById("result").innerText = "❌ Upload failed: " + result.message;
        }

    } catch (err) {
        document.getElementById("result").innerText = "❌ Request failed: " + err;
    }
}