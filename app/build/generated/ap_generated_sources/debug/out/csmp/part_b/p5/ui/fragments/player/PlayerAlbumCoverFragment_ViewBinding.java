// Generated code from Butter Knife. Do not modify!
package csmp.part_b.p5.ui.fragments.player;

import android.view.View;
import android.widget.ImageView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.viewpager.widget.ViewPager;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import csmp.part_b.p5.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class PlayerAlbumCoverFragment_ViewBinding implements Unbinder {
  private PlayerAlbumCoverFragment target;

  @UiThread
  public PlayerAlbumCoverFragment_ViewBinding(PlayerAlbumCoverFragment target, View source) {
    this.target = target;

    target.viewPager = Utils.findRequiredViewAsType(source, R.id.player_album_cover_viewpager, "field 'viewPager'", ViewPager.class);
    target.favoriteIcon = Utils.findRequiredViewAsType(source, R.id.player_favorite_icon, "field 'favoriteIcon'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    PlayerAlbumCoverFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.viewPager = null;
    target.favoriteIcon = null;
  }
}
