package com.gmpire.guruklub.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmpire.guruklub.view.activity.Registration.RegistrationViewModel
import com.gmpire.guruklub.view.activity.categoryAndSubjectSelection.CategoryAndSubjectSelectionViewModel
import com.gmpire.guruklub.view.activity.emailVerification.EmailVerificationViewModel
import com.gmpire.guruklub.view.activity.forgetPassword.ForgetPasswordViewModel
import com.gmpire.guruklub.view.activity.gameActivity.GameActivityViewModel
import com.gmpire.guruklub.view.activity.gameChallenge.GameChallengeQuestionViewModel
import com.gmpire.guruklub.view.activity.gameHelp.ContentViewModel
import com.gmpire.guruklub.view.activity.gameLevelQuestion.GameLevelQuestionViewModel
import com.gmpire.guruklub.view.activity.gameResultActivity.GameResultActivityViewModel
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelViewModel
import com.gmpire.guruklub.view.activity.gamesubscribe.SubscribeActivityViewModel
import com.gmpire.guruklub.view.activity.infoCenter.InfoCenterViewModel
import com.gmpire.guruklub.view.activity.login.LoginViewModel
import com.gmpire.guruklub.view.activity.main.MainViewModel
import com.gmpire.guruklub.view.activity.phoneVerification.PhoneVerificationViewModel
import com.gmpire.guruklub.view.activity.profileSetup.ProfileSetupViewModel
import com.gmpire.guruklub.view.activity.termsAndCondition.TermsAndConditionsViewModel
import com.gmpire.guruklub.view.base.BaseViewmodelFactory
import com.gmpire.guruklub.view.fragment.home.HomeFragmentViewModel
import com.gmpire.guruklub.view.activity.library.LibraryViewmodel
import com.gmpire.guruklub.view.activity.library.SingleVideoViewModel
import com.gmpire.guruklub.view.activity.modelTestActivity.ModelTestViewModel
import com.gmpire.guruklub.view.activity.profile.ProfileViewModel
import com.gmpire.guruklub.view.activity.question.QuestionViewModel
import com.gmpire.guruklub.view.activity.questionSearch.QuestionSearchViewModel
import com.gmpire.guruklub.view.activity.resetPassword.ResetPasswordViewModel
import com.gmpire.guruklub.view.activity.settings.SettingsViewModel
import com.gmpire.guruklub.view.activity.subjectBasedPerformace.SubjectBasedPerformanceViewModel
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionsViewModel
import com.gmpire.guruklub.view.fragment.favourite.FavouriteViewModel
import com.gmpire.guruklub.view.fragment.game.GameFragmentViewModel
import com.gmpire.guruklub.view.fragment.notification.NotificationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: BaseViewmodelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun postLoginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegistrationViewModel::class)
    internal abstract fun postRegistrationViewModel(viewModel: RegistrationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForgetPasswordViewModel::class)
    internal abstract fun postForgetPasswordViewModel(viewModel: ForgetPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileSetupViewModel::class)
    internal abstract fun postProfileSetupViewModel(viewModel: ProfileSetupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EmailVerificationViewModel::class)
    internal abstract fun postEmailVerificationViewModel(viewModel: EmailVerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TermsAndConditionsViewModel::class)
    internal abstract fun postTermsAndConditionsViewModel(viewModel: TermsAndConditionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContentViewModel::class)
    internal abstract fun postGameHelpViewModel(viewModel: ContentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PhoneVerificationViewModel::class)
    internal abstract fun postPhoneVerificationViewModel(viewModel: PhoneVerificationViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(CategoryAndSubjectSelectionViewModel::class)
    internal abstract fun postCategoryAndSubjectSelectionViewModel(viewModel: CategoryAndSubjectSelectionViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun postMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeFragmentViewModel::class)
    internal abstract fun postHomeFragmentViewModel(viewModel: HomeFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LibraryViewmodel::class)
    internal abstract fun postLibraryViewmodel(viewModel: LibraryViewmodel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ResetPasswordViewModel::class)
    internal abstract fun postResetPasswordViewModel(viewModel: ResetPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameResultActivityViewModel::class)
    internal abstract fun postGameResultActivityViewModel(viewModel: GameResultActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubjectBasedPerformanceViewModel::class)
    internal abstract fun postSubjectBasedPerformanceViewModel(viewModel: SubjectBasedPerformanceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameActivityViewModel::class)
    internal abstract fun postGameActivityViewModel(viewModel: GameActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameFragmentViewModel::class)
    internal abstract fun postGameFragmentViewModel(viewModel: GameFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QuestionViewModel::class)
    internal abstract fun QuestionViewModel(viewModel: QuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    internal abstract fun ProfileViewMoled(viewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewSolutionsViewModel::class)
    internal abstract fun ViewSolutionsViewModel(viewModel: ViewSolutionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavouriteViewModel::class)
    internal abstract fun FavouriteViewModel(viewModel: FavouriteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InfoCenterViewModel::class)
    internal abstract fun InfoCenterViewModel(viewModel: InfoCenterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ModelTestViewModel::class)
    internal abstract fun ModelTestViewModel(viewModel: ModelTestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    internal abstract fun NotificationViewModel(viewModel: NotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    internal abstract fun SettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QuestionSearchViewModel::class)
    internal abstract fun QuestionSearchViewModel(viewModel: QuestionSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SingleVideoViewModel::class)
    internal abstract fun SingleVideoViewModel(viewModel: SingleVideoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameLevelViewModel::class)
    internal abstract fun GameLevelViewModel(viewModel: GameLevelViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameLevelQuestionViewModel::class)
    internal abstract fun GameLevelQuestionViewModel(viewModel: GameLevelQuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameChallengeQuestionViewModel::class)
    internal abstract fun GameChallengeQuestionViewModel(viewModel: GameChallengeQuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscribeActivityViewModel::class)
    internal abstract fun SubscribeActivityViewModel(viewModel: SubscribeActivityViewModel): ViewModel


}