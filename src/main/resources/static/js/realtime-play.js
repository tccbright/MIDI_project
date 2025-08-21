(()=>{"use strict";

    /** ========= 映射：按你的规则 =========
     * Piano:  prog == 0 或 24..31（并额外显示 1..7 的钢琴/电钢）
     * Bass :  32..39
     * Violin: 40..47
     * Strings:48..55
     * Drums : 特例，通道固定 9（GM）
     */
    const PART_VARIANTS = {
        Piano: [
            {prog:0, name:"Acoustic Grand Piano"},
            {prog:1, name:"Bright Acoustic Piano"},
            {prog:2, name:"Electric Grand Piano"},
            {prog:3, name:"Honky-tonk Piano"},
            {prog:4, name:"Electric Piano 1"},
            {prog:5, name:"Electric Piano 2"},
            {prog:6, name:"Harpsichord"},
            {prog:7, name:"Clavi"},
        ],
        Bass: [
            {prog:32, name:"Acoustic Bass"},
            {prog:33, name:"Electric Bass (finger)"},
            {prog:34, name:"Electric Bass (pick)"},
            {prog:35, name:"Fretless Bass"},
            {prog:36, name:"Slap Bass 1"},
            {prog:37, name:"Slap Bass 2"},
            {prog:38, name:"Synth Bass 1"},
            {prog:39, name:"Synth Bass 2"},
        ],
        Violin: [
            {prog:40, name:"Violin"},
            {prog:41, name:"Viola"},
            {prog:42, name:"Cello"},
            {prog:43, name:"Contrabass"},
            {prog:44, name:"Tremolo Strings"},
            {prog:45, name:"Pizzicato Strings"},
            {prog:46, name:"Orchestral Harp"},
            {prog:47, name:"Timpani"},
        ],
        Strings: [
            {prog:48, name:"String Ensemble 1"},
            {prog:49, name:"String Ensemble 2"},
            {prog:50, name:"Synth Strings 1"},
            {prog:51, name:"Synth Strings 2"},
            {prog:52, name:"Choir Aahs"},
            {prog:53, name:"Voice Oohs"},
            {prog:54, name:"Synth Choir"},
            {prog:55, name:"Orchestra Hit"},
        ],
        Drums: [
            { prog: 0, name: "Standard Drum Kit (Ch 9)" },
            { prog: 8, name: "Room Kit" },
            { prog: 16, name: "Power Kit" },
            { prog: 24, name: "Electronic Kit" },
            { prog: 25, name: "TR-808 Kit" },
            { prog: 32, name: "Jazz Kit" },
            { prog: 40, name: "Brush Kit" },
            { prog: 48, name: "Orchestra Kit" },
            { prog: 56, name: "SFX Kit" }
        ]
        ,
    };

    /** ========= 主题路由（五个不同主题） ========= */
    const TOPIC_PROGRAM = {
        Piano:   "midi/piano/synth/program",
        Bass:    "midi/bass/synth/program",
        Violin:  "midi/violin/synth/program",
        Strings: "midi/strings/synth/program",
        Drums:   "midi/drums/synth/program",
    };
    const TOPIC_NOTEON = {
        Piano:   "midi/piano/synth/noteon",
        Bass:    "midi/bass/synth/noteon",
        Violin:  "midi/violin/synth/noteon",
        Strings: "midi/strings/synth/noteon",
        Drums:   "midi/drums/synth/noteon",
    };
    const TOPIC_NOTEOFF = {
        Piano:   "midi/piano/synth/noteoff",
        Bass:    "midi/bass/synth/noteoff",
        Violin:  "midi/violin/synth/noteoff",
        Strings: "midi/strings/synth/noteoff",
        Drums:   "midi/drums/synth/noteoff",
    };

    /** ========= 可调参数 ========= */
    const API_BASE = "http://172.20.10.10:8080"; // 你的后端
    const BANK_DEFAULT = 0;                       // bank 不用管，固定 0（要改就改这里）

    /** ========= 发送封装 ========= */
    async function sendMqtt(topic, payload){
        try{
            const res = await fetch(`${API_BASE}/mqtt/publish`,{
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ topic, payload })
            });
            if(!res.ok) throw new Error('HTTP '+res.status);
            console.log('✅ MQTT', topic, payload);
        }catch(e){
            console.error('❌ MQTT 失败', e);
            alert('MQTT 发送失败：'+e.message);
        }
    }

    /** ========= 交互逻辑 ========= */
    let currentPart = 'Piano';
    let currentProg = PART_VARIANTS.Piano[0].prog;

    const qs = s=>document.querySelector(s);
    const qsa = s=>Array.from(document.querySelectorAll(s));

    function renderVariants(){
        const box = qs('#variantList');
        if(!box) return;
        box.innerHTML = '';
        (PART_VARIANTS[currentPart]||[]).forEach(v=>{
            const btn = document.createElement('button');
            btn.className = 'variant';
            btn.textContent = `#${v.prog} ${v.name}`;
            btn.dataset.prog = v.prog;
            btn.addEventListener('click', ()=>{
                currentProg = Number(v.prog);
                qsa('.variant').forEach(x=>x.classList.remove('selected'));
                btn.classList.add('selected');
                const sel = qs('#currentSel');
                if(sel) sel.textContent = `已选音色：#${v.prog} ${v.name}`;
            });
            box.appendChild(btn);
        });
        const first = box.querySelector('.variant');
        first && first.click();
    }

    function updateChannelLock(){
        const progCh = qs('#progCh');
        const noteCh = qs('#noteCh');
        const hint = qs('#progChHint');
        if(currentPart==='Drums'){
            if(progCh){progCh.value=9; progCh.disabled=true;}
            if(noteCh){noteCh.value=9; noteCh.disabled=true;}
            if(hint) hint.textContent='Drums：通道固定为 9';
        }else{
            if(progCh){progCh.disabled=false; if(progCh.value==9) progCh.value=0;}
            if(noteCh){noteCh.disabled=false; if(noteCh.value==9) noteCh.value=0;}
            if(hint) hint.textContent='提示：Drums 自动使用通道 9';
        }
    }

    function bindPartTabs(){
        qsa('.part-btn').forEach(b=>{
            b.addEventListener('click', ()=>{
                qsa('.part-btn').forEach(x=>x.classList.remove('active'));
                b.classList.add('active');
                currentPart = b.dataset.part || 'Piano';
                renderVariants();
                updateChannelLock();
            });
        });
    }

    /** ========== 设置乐器（program） ==========
     * 主题：TOPIC_PROGRAM[currentPart]
     * 载荷："(BANK_DEFAULT,ch,prog)" —— 带括号
     */
    function bindProgramButton(){
        const btn = qs('#btnSetProgram');
        if(!btn) return;
        btn.addEventListener('click', ()=>{
            const ch = Number(qs('#progCh')?.value || 0);
            const topic = TOPIC_PROGRAM[currentPart];
            const payload = `(${BANK_DEFAULT},${ch},${currentProg})`;
            sendMqtt(topic, payload);
        });
    }

    /** ========== Note On / Off ==========
     * 主题：TOPIC_NOTEON / TOPIC_NOTEOFF（按 currentPart）
     * 载荷："(ch,pitch,vel)" —— 带括号
     */
    function bindNoteButtons(){
        const onBtn = qs('#btnNoteOn');
        const offBtn = qs('#btnNoteOff');
        onBtn && onBtn.addEventListener('click', ()=>{
            const ch = Number(qs('#noteCh')?.value || 0);
            const pitch = Number(qs('#notePitch')?.value || 60);
            const vel = Number(qs('#noteVel')?.value || 100);
            const topic = TOPIC_NOTEON[currentPart];
            const payload = `(${ch},${pitch},${vel})`;
            sendMqtt(topic, payload);
        });
        offBtn && offBtn.addEventListener('click', ()=>{
            const ch = Number(qs('#noteCh')?.value || 0);
            const pitch = Number(qs('#notePitch')?.value || 60);
            const topic = TOPIC_NOTEOFF[currentPart];
            const payload = `(${ch},${pitch},0)`;
            sendMqtt(topic, payload);
        });
    }

// 初始化
    document.addEventListener('DOMContentLoaded', ()=>{
        bindPartTabs();
        renderVariants();
        updateChannelLock();
        bindProgramButton();
        bindNoteButtons();
    });

})();
